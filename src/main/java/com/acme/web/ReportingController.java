package com.acme.web;

import com.acme.reporting.entity.DailySalesEntity;
import com.acme.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
public class ReportingController {

  private final ReportingService reportingService;

  @PostMapping("/daily-sales")
  public ResponseEntity<DailySalesEntity> upsertDailySales(@RequestBody Map<String, Object> body) {
    LocalDate date = LocalDate.parse(body.get("date").toString());
    long ordersCount = body.get("ordersCount") instanceof Number n ? n.longValue() : Long.parseLong(body.get("ordersCount").toString());
    BigDecimal grossAmount = body.get("grossAmount") instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : new BigDecimal(body.get("grossAmount").toString());
    DailySalesEntity created = reportingService.upsertDailySales(date, ordersCount, grossAmount);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/daily-sales/{date}")
  public ResponseEntity<DailySalesEntity> getDailySales(@PathVariable String date) {
    return ResponseEntity.ok(reportingService.getDailySales(LocalDate.parse(date)));
  }
}
