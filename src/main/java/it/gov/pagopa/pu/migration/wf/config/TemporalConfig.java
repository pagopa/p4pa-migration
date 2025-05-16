package it.gov.pagopa.pu.migration.wf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentracing.Tracer;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {
  public static final String TASK_QUEUE_MIGRATION = "MigrationTaskQueue";

  @Bean
  public Tracer globalTracer(OpenTelemetry openTelemetry){
    return OpenTracingShim.createTracerShim(openTelemetry);
  }

  @Bean
  public DataConverter temporalDataConverter(ObjectMapper objectMapper){
    return DefaultDataConverter.newDefaultInstance()
      .withPayloadConverterOverrides(new JacksonJsonPayloadConverter(objectMapper));
  }
}
