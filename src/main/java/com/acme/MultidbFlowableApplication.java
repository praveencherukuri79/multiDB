package com.acme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class
})
public class MultidbFlowableApplication {

  public static void main(String[] args) {
    SpringApplication.run(MultidbFlowableApplication.class, args);
  }
}
