package com.acme.config;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier for the DataSource used by Flowable (and primary JPA).
 * Flowable 7.x picks: 1) @FlowableDataSource, 2) @Primary, 3) bean name "dataSource".
 * In Flowable 7.2.0 the annotation is NOT in the starter (no org.flowable.spring.boot.flowable).
 * Use this until you upgrade to a Flowable version that provides:
 *   import org.flowable.spring.boot.flowable.FlowableDataSource;
 * Then remove this class and use that import in DataSourceConfig and PrimaryJpaConfig.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Qualifier("dataSource")
public @interface FlowableDataSource {
}
