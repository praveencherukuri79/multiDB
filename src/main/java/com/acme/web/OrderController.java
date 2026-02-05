package com.acme.web;

import com.acme.primary.entity.OrderEntity;
import com.acme.primary.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderEntity> create(@RequestBody Map<String, Object> body) {
    String orderNo = (String) body.get("orderNo");
    Long customerId = body.get("customerId") instanceof Number n ? n.longValue() : Long.parseLong(body.get("customerId").toString());
    BigDecimal totalAmount = body.get("totalAmount") instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : new BigDecimal(body.get("totalAmount").toString());
    OrderEntity created = orderService.createOrder(orderNo, customerId, totalAmount);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{orderNo}")
  public ResponseEntity<OrderEntity> getByOrderNo(@PathVariable String orderNo) {
    return ResponseEntity.ok(orderService.getByOrderNo(orderNo));
  }
}
