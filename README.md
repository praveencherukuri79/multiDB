# MultiDB Flowable Demo

A **Spring Boot 3** application demonstrating how to configure **multiple databases** (Primary, Reporting, Audit) alongside **Flowable Process Engine** — all in one application.

---

## What This App Does

This demo showcases a realistic enterprise scenario:

| Database | Purpose | Example Data |
|----------|---------|--------------|
| **Primary** | Main business data + Flowable process engine tables | Orders, process instances, tasks |
| **Reporting** | Read-optimized analytics/reporting data | Daily sales summaries |
| **Audit** | Compliance/audit trail logs | Audit events, timestamps |

### Key Features

- **3 separate H2 in-memory databases** — each with its own connection pool
- **Flowable BPMN process engine** — runs on the primary database
- **JPA/Hibernate** — separate EntityManagers per database
- **Transaction isolation** — each service uses its own transaction manager
- **REST endpoints** — to test each database independently

---

## Project Structure

```
src/main/java/com/acme/
├── MultidbFlowableApplication.java    # Main entry point
├── config/
│   ├── DataSourceConfig.java          # Defines 3 DataSource beans
│   ├── PrimaryJpaConfig.java          # JPA setup for primary DB
│   ├── ReportingJpaConfig.java        # JPA setup for reporting DB
│   └── AuditJpaConfig.java            # JPA setup for audit DB
├── primary/
│   ├── entity/OrderEntity.java        # Order entity (primary DB)
│   ├── repo/OrderRepository.java
│   └── service/OrderService.java
├── reporting/
│   ├── entity/DailySalesEntity.java   # Sales summary (reporting DB)
│   ├── repo/DailySalesRepository.java
│   └── service/ReportingService.java
├── audit/
│   ├── entity/AuditEventEntity.java   # Audit log (audit DB)
│   ├── repo/AuditEventRepository.java
│   └── service/AuditService.java
└── web/
    ├── OrderController.java           # REST API for orders
    ├── ReportingController.java       # REST API for reports
    ├── AuditController.java           # REST API for audit logs
    └── FlowableTestController.java    # REST API to test Flowable
```

---

## How Multi-Database Configuration Works

### Step 1: Disable Default DataSource Auto-Configuration

Spring Boot normally creates a single DataSource from `spring.datasource.*`. We disable this:

```java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
```

### Step 2: Define 3 DataSource Beans

In `DataSourceConfig.java`:

```java
@Bean(name = "dataSource")
@Primary                                              // Default for injections
@ConfigurationProperties(prefix = "app.datasource.primary")
public DataSource primaryDataSource() { ... }

@Bean(name = "reportingDataSource")
@ConfigurationProperties(prefix = "app.datasource.reporting")
public DataSource reportingDataSource() { ... }

@Bean(name = "auditDataSource")
@ConfigurationProperties(prefix = "app.datasource.audit")
public DataSource auditDataSource() { ... }
```

### Step 3: Create JPA Configuration Per Database

Each database gets its own:
- **EntityManagerFactory** — knows which entities and DataSource to use
- **TransactionManager** — manages transactions for that database
- **Repository scan** — wires repositories to the correct EntityManager

Example for primary:

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.acme.primary.repo",
    entityManagerFactoryRef = "primaryEmf",
    transactionManagerRef = "primaryTxManager"
)
public class PrimaryJpaConfig {
    @Bean @Primary
    public LocalContainerEntityManagerFactoryBean primaryEmf(...) { ... }

    @Bean @Primary
    public PlatformTransactionManager primaryTxManager(...) { ... }
}
```

### Step 4: Flowable Uses Primary Database

Flowable's auto-configuration looks for a DataSource by:
1. `@Primary` annotation
2. Bean named `"dataSource"`

Our primary DataSource has **both**, so Flowable automatically uses it. No special configuration needed.

### Step 5: Pin Services to Their Database

Each service specifies which transaction manager to use:

```java
@Service
@Transactional("primaryTxManager")      // Uses primary DB
public class OrderService { ... }

@Service
@Transactional("reportingTxManager")    // Uses reporting DB
public class ReportingService { ... }

@Service
@Transactional("auditTxManager")        // Uses audit DB
public class AuditService { ... }
```

---

## Database Configuration (application.yml)

```yaml
app:
  datasource:
    primary:
      jdbc-url: jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1
      username: sa
      password:
      driver-class-name: org.h2.Driver
      pool-name: primary-pool
    reporting:
      jdbc-url: jdbc:h2:mem:reporting;DB_CLOSE_DELAY=-1
      username: sa
      password:
      driver-class-name: org.h2.Driver
      pool-name: reporting-pool
    audit:
      jdbc-url: jdbc:h2:mem:audit;DB_CLOSE_DELAY=-1
      username: sa
      password:
      driver-class-name: org.h2.Driver
      pool-name: audit-pool
```

---

## Package Boundaries (Important!)

Entities **must** be in the correct package for their database:

| Database | Entity Package | Repository Package |
|----------|----------------|-------------------|
| Primary | `com.acme.primary.entity` | `com.acme.primary.repo` |
| Reporting | `com.acme.reporting.entity` | `com.acme.reporting.repo` |
| Audit | `com.acme.audit.entity` | `com.acme.audit.repo` |

⚠️ **Entities outside these packages won't be managed by any EntityManager!**

---

## Flowable Process Engine

Flowable is configured to:
- Use the **primary database** for all process engine tables (ACT_*)
- Auto-update database schema on startup
- Enable async job executor

A sample BPMN process is included: `src/main/resources/processes/demo-process.bpmn20.xml`

---

## Running the Application

```bash
mvn spring-boot:run
```

**Test endpoints:**

| Endpoint | Description |
|----------|-------------|
| `GET /orders` | List orders (primary DB) |
| `POST /orders` | Create order (primary DB) |
| `GET /reports/daily-sales` | List daily sales (reporting DB) |
| `GET /audit/events` | List audit events (audit DB) |
| `POST /flowable/start` | Start a Flowable process |
| `GET /flowable/tasks` | List active tasks |

**Test page:** http://localhost:8080/test.html

---

## Summary

| Concern | Solution |
|---------|----------|
| Multiple DataSources | `DataSourceConfig` with 3 `@Bean` methods |
| Flowable DataSource | `@Primary` + bean name `"dataSource"` |
| Separate JPA per DB | One config class per DB with own EMF + TxManager |
| Transaction isolation | `@Transactional("specificTxManager")` per service |
| Entity mapping | Package-based: each DB scans its own entity package |

---

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Flowable 7.2.0
- H2 Database (in-memory)
- HikariCP connection pool
- Spring Data JPA / Hibernate
