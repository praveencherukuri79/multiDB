package com.acme.web;

import com.acme.audit.entity.AuditEventEntity;
import com.acme.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

  private final AuditService auditService;

  @PostMapping("/events")
  public ResponseEntity<AuditEventEntity> writeEvent(@RequestBody Map<String, Object> body) {
    String eventType = (String) body.get("eventType");
    String actor = (String) body.get("actor");
    String subjectId = body.containsKey("subjectId") ? body.get("subjectId").toString() : null;
    String payloadJson = body.containsKey("payloadJson") ? body.get("payloadJson").toString() : null;
    AuditEventEntity created = auditService.writeEvent(eventType, actor, subjectId, payloadJson);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/events/{actor}")
  public ResponseEntity<List<AuditEventEntity>> last50ForActor(@PathVariable String actor) {
    return ResponseEntity.ok(auditService.last50ForActor(actor));
  }
}
