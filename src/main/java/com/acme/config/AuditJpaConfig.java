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
    basePackages = "com.acme.audit.repo",
    entityManagerFactoryRef = "auditEmf",
    transactionManagerRef = "auditTxManager"
)
@EntityScan(basePackages = "com.acme.audit.entity")
public class AuditJpaConfig {

  @Bean
  public LocalContainerEntityManagerFactoryBean auditEmf(
      EntityManagerFactoryBuilder builder,
      @Qualifier("auditDataSource") DataSource ds) {
    return builder
        .dataSource(ds)
        .packages("com.acme.audit.entity")
        .persistenceUnit("auditPU")
        .build();
  }

  @Bean
  public PlatformTransactionManager auditTxManager(
      @Qualifier("auditEmf") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
