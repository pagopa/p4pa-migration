package it.gov.pagopa.pu.migration.wf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "workflow.default")
@Data
public class BaseWfConfig {
  protected int startToCloseTimeoutInSeconds;
  protected int retryInitialIntervalInMillis;
  protected double retryBackoffCoefficient;
  protected int retryMaximumAttempts;
}
