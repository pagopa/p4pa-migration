package it.gov.pagopa.pu.migration.wf.config;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkflowImplementationOptions;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.Duration;

@Service
public class TemporalWFImplementationCustomizer implements TemporalOptionsCustomizer<WorkflowImplementationOptions.Builder> {

  private final BaseWfConfig defaultConfig;

  public TemporalWFImplementationCustomizer(
    @Qualifier("baseWfConfig") BaseWfConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  @Nonnull
  @Override
  public WorkflowImplementationOptions.Builder customize(@Nonnull WorkflowImplementationOptions.Builder optionsBuilder) {
    return optionsBuilder
      .setDefaultActivityOptions(baseWfConfig2ActivityOptions(defaultConfig));
  }

  public static ActivityOptions baseWfConfig2ActivityOptions(BaseWfConfig baseWfConfig) {
    return baseWfConfig2ActivityOptions(null, baseWfConfig);
  }

  public static ActivityOptions baseWfConfig2ActivityOptions(String taskQueue, BaseWfConfig baseWfConfig) {
    return ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofSeconds(baseWfConfig.getStartToCloseTimeoutInSeconds()))
      .setTaskQueue(taskQueue)
      .setRetryOptions(
        RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofMillis(baseWfConfig.getRetryInitialIntervalInMillis()))
          .setBackoffCoefficient(baseWfConfig.getRetryBackoffCoefficient())
          .setDoNotRetry(IllegalArgumentException.class.getName(), NotRetryableActivityException.class.getName())
          .setMaximumAttempts(baseWfConfig.getRetryMaximumAttempts())
          .build())
      .build();
  }
}
