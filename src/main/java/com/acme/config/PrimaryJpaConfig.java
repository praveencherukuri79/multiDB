package com.acme.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.acme.primary.repo",
    entityManagerFactoryRef = "primaryEmf",
    transactionManagerRef = "primaryTxManager"
)
@EntityScan(basePackages = "com.acme.primary.entity")
public class PrimaryJpaConfig {

  /**
   * Qualifier MUST match the primary DataSource bean name ("dataSource").
   * This is also the DataSource used by Flowable (via @Primary).
   */
  @Bean(name = "primaryEmf")
  @Primary
  public LocalContainerEntityManagerFactoryBean primaryEmf(
      EntityManagerFactoryBuilder builder,
      @Qualifier("dataSource") DataSource primaryDataSource) {

    return builder
        .dataSource(primaryDataSource)
        .packages("com.acme.primary.entity")
        .persistenceUnit("primaryPU")
        .build();
  }

  @Bean(name = "primaryTxManager")
  @Primary
  public PlatformTransactionManager primaryTxManager(
      @Qualifier("primaryEmf") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
