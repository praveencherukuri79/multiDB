# Orchestrator Pattern Example

Complete working example demonstrating the **Orchestrator Pattern** for multi-database architectures.

---

## Architecture Overview

```
┌───────────────────────────────────────────┐
│   OrderOrchestratorController (REST)     │
└───────────────┬───────────────────────────┘
                │
                ↓
┌───────────────────────────────────────────┐
│   OrderOrchestrator                       │  ← NO @Transactional
│   (Business Logic & Coordination)         │     Coordinates workflows
└───────┬───────────────┬───────────────────┘
        │               │               
        ↓               ↓               ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│OrderService │  │AuditService │  │ReportingSrv │
│@Txn(primary)│  │@Txn(audit)  │  │@Txn(report) │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       ↓                ↓                ↓
  [Primary DB]     [Audit DB]      [Reporting DB]
```

---

## What Was Created

### 1. Orchestrator Service
**File:** `com.acme.orchestrator.OrderOrchestrator`

- **NO @Transactional** annotation
- Coordinates business workflows across multiple databases
- Handles validation, error handling, and compensation
- Calls transactional services for database operations

### 2. Updated Transactional Services

#### OrderService (Primary DB)
- `createOrder()` - Create order with `@Transactional("primaryTxManager")`
- `deleteOrder()` - Delete order (for compensation)
- `getOrderById()` - Read order

#### AuditService (Audit DB)
- `logOrderPlaced()` - Log order event with `@Transactional("auditTxManager")`
- `logOrderCancelled()` - Log cancellation (for compensation)
- `getOrderHistory()` - Read audit logs

#### ReportingService (Reporting DB)
- `recordSale()` - Update metrics with `@Transactional("reportingTxManager")`
- `getDailySales()` - Read daily sales

### 3. REST Controller
**File:** `com.acme.web.OrderOrchestratorController`

Exposes endpoints that call the orchestrator (not individual services).

---

## How It Works

### Transaction Flow: Place Order

```
POST /api/orchestrator/orders
{
  "customerName": "John Doe",
  "productName": "Laptop",
  "amount": 1299.99
}
```

**Execution:**
```
OrderOrchestratorController.placeOrder()
  ↓
OrderOrchestrator.placeOrder()
  ↓
  ├─ OrderService.createOrder()
  │    BEGIN Primary Transaction
  │    INSERT INTO orders VALUES (...)
  │    COMMIT Primary Transaction ✓
  ↓
  ├─ AuditService.logOrderPlaced()
  │    BEGIN Audit Transaction
  │    INSERT INTO audit_event VALUES (...)
  │    COMMIT Audit Transaction ✓
  ↓
  └─ ReportingService.recordSale()
       BEGIN Reporting Transaction
       UPDATE daily_sales SET orders_count = orders_count + 1
       COMMIT Reporting Transaction ✓

Result: 3 separate, independent transactions - all committed
```

---

## API Endpoints

### 1. Place Order (Eventual Consistency)
```bash
POST http://localhost:8080/api/orchestrator/orders
Content-Type: application/json

{
  "customerName": "John Doe",
  "productName": "Laptop",
  "amount": 1299.99
}
```

**Behavior:**
- Saves order to Primary DB (guaranteed)
- Logs to Audit DB (best effort - logged on failure)
- Updates Reporting DB (best effort - logged on failure)

### 2. Place Order with Saga (Compensation)
```bash
POST http://localhost:8080/api/orchestrator/orders/saga
Content-Type: application/json

{
  "customerName": "Jane Smith",
  "productName": "Phone",
  "amount": 899.99
}
```

**Behavior:**
- If ANY step fails, compensates (undoes) previous steps
- Stronger consistency guarantee
- Use when audit logging is critical

### 3. Get Order Details (Multi-DB Read)
```bash
GET http://localhost:8080/api/orchestrator/orders/1/details
```

**Response:**
```json
{
  "order": {
    "id": 1,
    "orderNo": "ORD-1234567890",
    "totalAmount": 1299.99,
    "createdAt": "2026-02-07T10:30:00Z"
  },
  "auditHistory": "2026-02-07T10:30:00Z - ORDER_PLACED by John Doe\n"
}
```

### 4. Get Single Order
```bash
GET http://localhost:8080/api/orchestrator/orders/1
```

---

## Key Benefits

| Benefit | Description |
|---------|-------------|
| **Separation of Concerns** | Business logic (orchestrator) separate from data access (services) |
| **No Circular Dependencies** | Clean hierarchy: Controller → Orchestrator → Services |
| **Transaction Clarity** | Each service manages exactly ONE transaction for ONE database |
| **Testability** | Mock services in orchestrator tests, mock repos in service tests |
| **Reusability** | Services can be reused by different orchestrators |
| **Compensation Logic** | Centralized error handling and compensation |

---

## Design Rules

### ✅ DO

1. **Orchestrator has NO @Transactional** - coordinates but doesn't manage transactions
2. **Each service has explicit @Transactional("xxxTxManager")**
3. **Services are database-specific** - one service per database
4. **Controller calls orchestrator only** - never calls services directly
5. **Handle failures gracefully** - try/catch with compensation or logging
6. **Services never call other services** - only orchestrator coordinates

### ❌ DON'T

1. Don't put @Transactional on orchestrator (can't span multiple DBs)
2. Don't let services call other services (creates circular dependencies)
3. Don't put business logic in services (keep them focused on data access)
4. Don't call repositories from orchestrator (always go through services)

---

## Testing the Orchestrator

### Start the Application
```bash
mvn spring-boot:run
```

### Test Order Placement
```bash
curl -X POST http://localhost:8080/api/orchestrator/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice",
    "productName": "Laptop",
    "amount": 1299.99
  }'
```

### Check Logs
Watch for log messages showing:
- ✓ Order saved to Primary DB
- ✓ Audit log created in Audit DB
- ✓ Sales metrics updated in Reporting DB

### Test Saga Pattern
```bash
curl -X POST http://localhost:8080/api/orchestrator/orders/saga \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Bob",
    "productName": "Phone",
    "amount": 899.99
  }'
```

### Get Order Details
```bash
curl http://localhost:8080/api/orchestrator/orders/1/details
```

---

## Comparison: With vs Without Orchestrator

### Without Orchestrator (Anti-Pattern)
```java
@RestController
public class OrderController {
    @Autowired OrderService orderService;
    @Autowired AuditService auditService;
    @Autowired ReportingService reportingService;
    
    @PostMapping("/orders")
    public Order placeOrder(@RequestBody OrderRequest req) {
        // ❌ Business logic in controller
        // ❌ Controller knows about 3 databases
        Order order = orderService.createOrder(...);
        auditService.logOrderPlaced(...);
        reportingService.recordSale(...);
        return order;
    }
}
```

### With Orchestrator (Correct Pattern)
```java
@RestController
public class OrderOrchestratorController {
    @Autowired OrderOrchestrator orchestrator;
    
    @PostMapping("/orders")
    public Order placeOrder(@RequestBody OrderRequest req) {
        // ✅ Delegates to orchestrator
        // ✅ Controller doesn't know about databases
        return orchestrator.placeOrder(...);
    }
}
```

---

## Next Steps

1. **Run the application** and test the endpoints
2. **Check the logs** to see the transaction flow
3. **Try the saga endpoint** to see compensation in action
4. **Extend the pattern** to other business workflows (customer registration, etc.)

---

## Summary

The **Orchestrator Pattern** provides:
- ✅ Clean separation between business logic and data access
- ✅ Each transaction scoped to exactly one database
- ✅ No circular dependencies
- ✅ Easy to test and maintain
- ✅ Ready for evolution to microservices

**This is the recommended pattern for multi-database Spring Boot applications!**
