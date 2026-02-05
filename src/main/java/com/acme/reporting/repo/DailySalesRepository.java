package com.acme.reporting.repo;

import com.acme.reporting.entity.DailySalesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySalesRepository extends JpaRepository<DailySalesEntity, Long> {
  Optional<DailySalesEntity> findBySalesDate(LocalDate salesDate);
}
