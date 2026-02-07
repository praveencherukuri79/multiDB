package com.acme.audit.service;

import com.acme.audit.entity.AuditEventEntity;
import com.acme.audit.repo.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional("auditTxManager")
public class AuditService {

  private final AuditEventRepository auditEventRepository;

  public AuditEventEntity writeEvent(String eventType, String actor, String subjectId, String payloadJson) {
    AuditEventEntity e = AuditEventEntity.builder()
        .eventType(eventType)
        .actor(actor)
        .subjectId(subjectId)
        .payloadJson(payloadJson)
        .createdAt(Instant.now())
        .build();
    return auditEventRepository.save(e);
  }

  @Transactional(readOnly = true)
  public List<AuditEventEntity> last50ForActor(String actor) {
    return auditEventRepository.findTop50ByActorOrderByCreatedAtDesc(actor);
  }

  /**
   * Log order placement for orchestrator demo.
   */
  public void logOrderPlaced(Long orderId, String customerName, BigDecimal amount) {
    String payloadJson = String.format("{\"orderId\":%d,\"amount\":\"%s\"}", orderId, amount);
    writeEvent("ORDER_PLACED", customerName, String.valueOf(orderId), payloadJson);
  }

  /**
   * Log order cancellation (for compensation).
   */
  public void logOrderCancelled(Long orderId, String reason) {
    writeEvent("ORDER_CANCELLED", "SYSTEM", String.valueOf(orderId), reason);
  }

  /**
   * Get audit history for an order.
   */
  @Transactional(readOnly = true)
  public String getOrderHistory(Long orderId) {
    List<AuditEventEntity> events = auditEventRepository.findBySubjectId(String.valueOf(orderId));
    StringBuilder history = new StringBuilder();
    for (AuditEventEntity event : events) {
      history.append(String.format("%s - %s by %s\n",
          event.getCreatedAt(), event.getEventType(), event.getActor()));
    }
    return history.toString();
  }
}
