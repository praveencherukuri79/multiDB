package com.acme.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

  /**
   * Primary DB – also used by Flowable process engine.
   * Flowable uses this DataSource via @Primary / bean name "dataSource".
   */
  @Bean(name = "dataSource")
  @Primary
  @ConfigurationProperties(prefix = "app.datasource.primary")
  public DataSource primaryDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Bean(name = "reportingDataSource")
  @ConfigurationProperties(prefix = "app.datasource.reporting")
  public DataSource reportingDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Bean(name = "auditDataSource")
  @ConfigurationProperties(prefix = "app.datasource.audit")
  public DataSource auditDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }
}
