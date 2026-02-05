package com.acme.reporting.service;

import com.acme.reporting.entity.DailySalesEntity;
import com.acme.reporting.repo.DailySalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional("reportingTxManager")
public class ReportingService {

  private final DailySalesRepository dailySalesRepository;

  public DailySalesEntity upsertDailySales(LocalDate date, long ordersCount, BigDecimal grossAmount) {
    DailySalesEntity entity = dailySalesRepository.findBySalesDate(date)
        .orElseGet(() -> DailySalesEntity.builder().salesDate(date).build());
    entity.setOrdersCount(ordersCount);
    entity.setGrossAmount(grossAmount);
    return dailySalesRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public DailySalesEntity getDailySales(LocalDate date) {
    return dailySalesRepository.findBySalesDate(date)
        .orElseThrow(() -> new IllegalArgumentException("No daily sales for: " + date));
  }
}
