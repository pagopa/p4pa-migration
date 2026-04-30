package it.gov.pagopa.pu.migration.wf.config.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelTraceContextBuilder;
import io.opentelemetry.opentracingshim.SpanShimHolder;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * An ObservationHandler that creates a Micrometer Tracing Span from the current OpenTracing Span if no Micrometer Span is present in the Observation context.
 */
@Service
public class OpenTracingShimObservationHandler implements ObservationHandler<Observation.Context> {

  private final List<String> correlationFields;
  private final Tracer tracer;

  public OpenTracingShimObservationHandler(TracingProperties tracingProperties, @Lazy Tracer tracer) {
    this.correlationFields = tracingProperties.getBaggage().getCorrelation().getFields();
    this.tracer = tracer;
  }

  @Override
  public boolean supportsContext(Observation.Context context) {
    Span currentSpan = SpanShimHolder.getCurrentSpan();
    if(currentSpan != null && tracer.currentSpan() == null) {
      SpanContext currentSpanContext = currentSpan.context();
      io.micrometer.tracing.Span.Builder otSpanBuilder = tracer.spanBuilder()
        .setParent(new OtelTraceContextBuilder()
          .parentId(currentSpanContext.toTraceId())
          .traceId(currentSpanContext.toTraceId())
          .spanId(currentSpanContext.toSpanId())
          .sampled(false)
          .build());

      currentSpanContext.baggageItems().forEach(i -> {
        if (correlationFields.contains(i.getKey())) {
          otSpanBuilder.tag(i.getKey(), i.getValue());
        }
      });

      if (context.get(TracingObservationHandler.TracingContext.class) == null) {
        context.put(TracingObservationHandler.TracingContext.class, new TracingObservationHandler.TracingContext() {
          @Override
          public io.micrometer.tracing.Span getSpan() {
            return otSpanBuilder.start();
          }
        });
      }
    }
    return false;
  }
}
