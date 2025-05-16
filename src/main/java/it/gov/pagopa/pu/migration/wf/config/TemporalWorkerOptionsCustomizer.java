package it.gov.pagopa.pu.migration.wf.config;

import io.temporal.spring.boot.WorkerOptionsCustomizer;
import io.temporal.worker.WorkerOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class TemporalWorkerOptionsCustomizer implements WorkerOptionsCustomizer {

  @Nonnull
  @Override
  public WorkerOptions.Builder customize(@Nonnull WorkerOptions.Builder optionsBuilder) {
    return optionsBuilder.setUsingVirtualThreads(true);
  }

  @Nonnull
  @Override
  public WorkerOptions.Builder customize(@Nonnull WorkerOptions.Builder optionsBuilder, @Nonnull String workerName, @Nonnull String taskQueue) {
    return optionsBuilder;
  }
}
