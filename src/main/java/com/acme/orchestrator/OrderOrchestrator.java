package com.acme.orchestrator;

import com.acme.primary.entity.OrderEntity;
import com.acme.primary.service.OrderService;
import com.acme.audit.service.AuditService;
import com.acme.reporting.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Orchestrator Service - Coordinates multi-DB business workflows.
 * 
 * KEY CHARACTERISTICS:
 * - NO @Transactional annotation (doesn't manage transactions)
 * - Calls transactional services that each handle their own DB
 * - Each service call is a separate, independent transaction
 * - Handles business logic, validation, error handling, compensation
 * 
 * DEPENDENCIES:
 * - OrderService (Primary DB) - transactional operations
 * - AuditService (Audit DB) - transactional logging
 * - ReportingService (Reporting DB) - transactional metrics
 */
@Service
public class OrderOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderOrchestrator.class);

    private final OrderService orderService;
    private final AuditService auditService;
    private final ReportingService reportingService;

    public OrderOrchestrator(OrderService orderService,
                            AuditService auditService,
                            ReportingService reportingService) {
        this.orderService = orderService;
        this.auditService = auditService;
        this.reportingService = reportingService;
    }

    /**
     * Complete order placement workflow across 3 databases.
     * 
     * Transaction Flow:
     * 1. OrderService.createOrder() → BEGIN Primary TX → INSERT order → COMMIT
     * 2. AuditService.logOrderPlaced() → BEGIN Audit TX → INSERT audit_log → COMMIT
     * 3. ReportingService.recordSale() → BEGIN Reporting TX → UPDATE metrics → COMMIT
     * 
     * Each step is a separate transaction. If step 3 fails, steps 1 & 2 are already committed.
     * This is "eventual consistency" - we accept temporary inconsistency for better performance.
     */
    public OrderEntity placeOrder(String customerName, String productName, BigDecimal amount) {
        log.info("Starting order placement workflow - customer: {}, product: {}, amount: {}",
                customerName, productName, amount);

        // Validate business rules (orchestrator responsibility)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }

        // STEP 1: Save order to Primary DB
        // This is a separate transaction managed by OrderService
        OrderEntity order = orderService.createOrder(customerName, productName, amount);
        log.info("✓ Order saved to Primary DB with ID: {}", order.getId());

        // STEP 2: Log to Audit DB
        // This is a separate transaction managed by AuditService
        try {
            auditService.logOrderPlaced(order.getId(), customerName, amount);
            log.info("✓ Audit log created in Audit DB for order: {}", order.getId());
        } catch (Exception e) {
            // Audit failed, but order is already saved
            // Log error and continue - we can retry audit logging later
            log.error("✗ Failed to create audit log for order: {} (order is still saved)", 
                    order.getId(), e);
        }

        // STEP 3: Update reporting metrics
        // This is a separate transaction managed by ReportingService
        try {
            reportingService.recordSale(productName, amount);
            log.info("✓ Sales metrics updated in Reporting DB for product: {}", productName);
        } catch (Exception e) {
            // Reporting failed, but order is saved and audit is logged
            log.error("✗ Failed to update reporting for product: {} (order is still saved)", 
                    productName, e);
        }

        log.info("Order placement workflow completed for order ID: {}", order.getId());
        return order;
    }

    /**
     * Order placement with compensation (Saga pattern).
     * If any critical step fails, we compensate (undo) previous steps.
     * 
     * Use this when you need stronger consistency guarantees.
     */
    public OrderEntity placeOrderWithCompensation(String customerName, String productName, BigDecimal amount) {
        log.info("Starting order placement with compensation - customer: {}, product: {}",
                customerName, productName);

        OrderEntity savedOrder = null;
        boolean auditLogged = false;

        try {
            // Step 1: Create order in Primary DB
            savedOrder = orderService.createOrder(customerName, productName, amount);
            log.info("✓ Order saved with ID: {}", savedOrder.getId());

            // Step 2: Log to Audit DB (critical - must succeed)
            auditService.logOrderPlaced(savedOrder.getId(), customerName, amount);
            auditLogged = true;
            log.info("✓ Audit log created");

            // Step 3: Update Reporting DB
            reportingService.recordSale(productName, amount);
            log.info("✓ Reporting updated");

            return savedOrder;

        } catch (Exception e) {
            log.error("Order workflow failed, initiating compensation", e);

            // COMPENSATION: Undo what succeeded
            if (savedOrder != null) {
                try {
                    // Delete the order from Primary DB
                    orderService.deleteOrder(savedOrder.getId());
                    log.info("⚠ Compensated: deleted order {} from Primary DB", savedOrder.getId());
                } catch (Exception compEx) {
                    log.error("CRITICAL: Failed to compensate - order {} may be orphaned!", 
                            savedOrder.getId(), compEx);
                    // In production: send alert to operations team
                }
            }

            if (auditLogged) {
                try {
                    // Log compensation action
                    auditService.logOrderCancelled(savedOrder.getId(), "COMPENSATION: " + e.getMessage());
                    log.info("⚠ Compensated: logged cancellation in Audit DB");
                } catch (Exception compEx) {
                    log.error("Failed to log compensation in Audit DB", compEx);
                }
            }

            throw new RuntimeException("Order placement failed and was compensated: " + e.getMessage(), e);
        }
    }

    /**
     * Read-only workflow: Get order with enriched data from multiple DBs.
     * No transactions needed - just coordinating reads across databases.
     */
    public OrderDetailsDTO getOrderDetails(Long orderId) {
        log.info("Fetching order details from multiple DBs for order: {}", orderId);

        // Read from Primary DB
        OrderEntity order = orderService.getOrderById(orderId);

        // Read from Audit DB
        String auditHistory = auditService.getOrderHistory(orderId);

        // Read from Reporting DB (if needed - example)
        // Could fetch related metrics, customer analytics, etc.

        log.info("Order details fetched from all databases");
        return new OrderDetailsDTO(order, auditHistory);
    }

    /**
     * Business method that only touches one database.
     * Orchestrator just delegates to the appropriate service.
     */
    public OrderEntity getOrder(Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * DTO for enriched order details from multiple DBs
     */
    public static class OrderDetailsDTO {
        private final OrderEntity order;
        private final String auditHistory;

        public OrderDetailsDTO(OrderEntity order, String auditHistory) {
            this.order = order;
            this.auditHistory = auditHistory;
        }

        public OrderEntity getOrder() { return order; }
        public String getAuditHistory() { return auditHistory; }
    }
}
