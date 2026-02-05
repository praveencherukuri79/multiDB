package com.acme.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_sales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sales_date", nullable = false, unique = true)
  private LocalDate salesDate;

  @Column(name = "orders_count", nullable = false)
  private Long ordersCount;

  @Column(name = "gross_amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal grossAmount;
}
