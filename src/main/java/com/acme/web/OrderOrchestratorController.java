package com.acme.web;

import com.acme.orchestrator.OrderOrchestrator;
import com.acme.orchestrator.OrderOrchestrator.OrderDetailsDTO;
import com.acme.primary.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST Controller demonstrating Orchestrator Pattern.
 * 
 * KEY POINTS:
 * - Controller calls ONLY the orchestrator (not individual services)
 * - Orchestrator handles all cross-DB coordination
 * - Clean separation: Controller → Orchestrator → Services → Repositories → DBs
 */
@RestController
@RequestMapping("/api/orchestrator/orders")
public class OrderOrchestratorController {

    private static final Logger log = LoggerFactory.getLogger(OrderOrchestratorController.class);

    private final OrderOrchestrator orderOrchestrator;

    public OrderOrchestratorController(OrderOrchestrator orderOrchestrator) {
        this.orderOrchestrator = orderOrchestrator;
    }

    /**
     * Place order - multi-DB workflow with eventual consistency.
     * 
     * Flow:
     * 1. Saves order to Primary DB
     * 2. Logs to Audit DB (best effort)
     * 3. Updates Reporting DB (best effort)
     * 
     * Example:
     * POST /api/orchestrator/orders
     * {
     *   "customerName": "John Doe",
     *   "productName": "Laptop",
     *   "amount": 1299.99
     * }
     */
    @PostMapping
    public ResponseEntity<OrderEntity> placeOrder(@RequestBody OrderRequest request) {
        log.info("Received order placement request: {}", request);

        try {
            OrderEntity order = orderOrchestrator.placeOrder(
                    request.customerName(),
                    request.productName(),
                    request.amount()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (IllegalArgumentException e) {
            log.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to place order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Place order with compensation (Saga pattern).
     * Stronger consistency - if any step fails, previous steps are undone.
     * 
     * Example:
     * POST /api/orchestrator/orders/saga
     * {
     *   "customerName": "Jane Smith",
     *   "productName": "Phone",
     *   "amount": 899.99
     * }
     */
    @PostMapping("/saga")
    public ResponseEntity<?> placeOrderWithSaga(@RequestBody OrderRequest request) {
        log.info("Received saga order placement request: {}", request);

        try {
            OrderEntity order = orderOrchestrator.placeOrderWithCompensation(
                    request.customerName(),
                    request.productName(),
                    request.amount()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (RuntimeException e) {
            log.error("Saga failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Order placement failed and was compensated: " + e.getMessage()));
        }
    }

    /**
     * Get order details from multiple databases.
     * 
     * Example:
     * GET /api/orchestrator/orders/1/details
     * 
     * Response includes:
     * - Order data from Primary DB
     * - Audit history from Audit DB
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long id) {
        log.info("Fetching order details for ID: {}", id);

        try {
            OrderDetailsDTO details = orderOrchestrator.getOrderDetails(id);
            return ResponseEntity.ok(details);

        } catch (RuntimeException e) {
            log.error("Order not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get single order (single DB query - no orchestration needed).
     * 
     * Example:
     * GET /api/orchestrator/orders/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> getOrder(@PathVariable Long id) {
        log.info("Fetching order: {}", id);

        try {
            OrderEntity order = orderOrchestrator.getOrder(id);
            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {
            log.error("Order not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Request DTO for order placement
     */
    public record OrderRequest(
            String customerName,
            String productName,
            BigDecimal amount
    ) {}
}
