package com.acme.audit.service;

import com.acme.audit.entity.AuditEventEntity;
import com.acme.audit.repo.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
