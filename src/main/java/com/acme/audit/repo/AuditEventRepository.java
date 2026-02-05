package com.acme.audit.repo;

import com.acme.audit.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
  List<AuditEventEntity> findTop50ByActorOrderByCreatedAtDesc(String actor);
  List<AuditEventEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);
}
