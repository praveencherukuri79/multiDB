package com.acme.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.acme.reporting.repo",
    entityManagerFactoryRef = "reportingEmf",
    transactionManagerRef = "reportingTxManager"
)
@EntityScan(basePackages = "com.acme.reporting.entity")
public class ReportingJpaConfig {

  @Bean
  public LocalContainerEntityManagerFactoryBean reportingEmf(
      EntityManagerFactoryBuilder builder,
      @Qualifier("reportingDataSource") DataSource ds) {
    return builder
        .dataSource(ds)
        .packages("com.acme.reporting.entity")
        .persistenceUnit("reportingPU")
        .build();
  }

  @Bean
  public PlatformTransactionManager reportingTxManager(
      @Qualifier("reportingEmf") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
