package com.acme.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_type", nullable = false, length = 60)
  private String eventType;

  @Column(name = "actor", nullable = false, length = 120)
  private String actor;

  @Column(name = "subject_id", length = 100)
  private String subjectId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Lob
  @Column(name = "payload_json")
  private String payloadJson;
}
