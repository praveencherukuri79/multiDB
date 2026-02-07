package com.acme.primary.service;

import com.acme.primary.entity.OrderEntity;
import com.acme.primary.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional("primaryTxManager")
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderEntity createOrder(String orderNo, Long customerId, BigDecimal totalAmount) {
    if (orderRepository.existsByOrderNo(orderNo)) {
      throw new IllegalStateException("Order already exists: " + orderNo);
    }
    OrderEntity entity = OrderEntity.builder()
        .orderNo(orderNo)
        .customerId(customerId)
        .totalAmount(totalAmount)
        .createdAt(Instant.now())
        .build();
    return orderRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public OrderEntity getByOrderNo(String orderNo) {
    return orderRepository.findByOrderNo(orderNo)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
  }

  /**
   * Simplified order creation for orchestrator demo.
   */
  public OrderEntity createOrder(String customerName, String productName, BigDecimal amount) {
    String orderNo = "ORD-" + System.currentTimeMillis();
    OrderEntity entity = OrderEntity.builder()
        .orderNo(orderNo)
        .customerId(1L)  // Demo: fixed customer ID
        .totalAmount(amount)
        .createdAt(Instant.now())
        .build();
    return orderRepository.save(entity);
  }

  /**
   * Delete order (for compensation).
   */
  public void deleteOrder(Long orderId) {
    orderRepository.deleteById(orderId);
  }

  /**
   * Get order by ID.
   */
  @Transactional(readOnly = true)
  public OrderEntity getOrderById(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
  }
}
