# MultiDB + Flowable (Spring Boot 3.5.6, Flowable 7.2.0)

Example project with **3 H2 databases** (Primary, Reporting, Audit) and **Flowable process engine** (process-only starter) using the **primary** DB.

## Stack

- Spring Boot **3.5.6**
- Flowable **7.2.0** (`flowable-spring-boot-starter-process` only)
- **3 H2** in-memory datasources: `primary`, `reporting`, `audit`
- JPA per DB with separate packages and transaction managers

## Run

```bash
mvn spring-boot:run
```

- App: http://localhost:8080  
- Test page with curl examples: http://localhost:8080/test.html  

## Test endpoints (POST)

| DB / Feature | POST | Body (JSON) |
|--------------|------|-------------|
| **Primary** (orders) | `POST /api/orders` | `{"orderNo":"ORD-001","customerId":1001,"totalAmount":99.50}` |
| **Reporting** (daily sales) | `POST /api/reporting/daily-sales` | `{"date":"2025-02-05","ordersCount":42,"grossAmount":12500}` |
| **Audit** (events) | `POST /api/audit/events` | `{"eventType":"ORDER_CREATED","actor":"system","subjectId":"ORD-001","payloadJson":"{}"}` |
| **Flowable** (deploy) | `POST /api/flowable/deploy` | (none) |
| **Flowable** (start process) | `POST /api/flowable/process/start` | `{"processKey":"demoProcess"}` (optional) |

GET examples: `/api/orders/ORD-001`, `/api/reporting/daily-sales/2025-02-05`, `/api/audit/events/system`, `/api/flowable/deployments`, `/api/flowable/process/instances`.

## Layout

- **Primary DB** (Flowable + orders): `com.acme.primary.*`, tx: `transactionManager`
- **Reporting DB**: `com.acme.reporting.*`, tx: `reportingTxManager`
- **Audit DB**: `com.acme.audit.*`, tx: `auditTxManager`
- Datasources: `app.datasource.primary|reporting|audit` in `application.yml`
- Flowable uses the primary datasource (`@Primary` bean `dataSource`)
