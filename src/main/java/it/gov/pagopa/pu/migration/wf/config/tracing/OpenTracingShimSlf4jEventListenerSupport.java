package it.gov.pagopa.pu.migration.wf.config.tracing;

import io.micrometer.tracing.otel.bridge.EventListener;
import io.micrometer.tracing.otel.bridge.EventPublishingContextWrapper;
import io.opentelemetry.opentracingshim.SpanShimHolder;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.slf4j.MDC;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * An EventListener that replaces Slf4JEventListener MDC context configuration only if the span is null in the event.<BR />
 * Necessary because applicationInsight will prevent OTel to log the spans.
 */
@Service
public class OpenTracingShimSlf4jEventListenerSupport implements EventListener {

  private final List<String> correlationFields;

  public OpenTracingShimSlf4jEventListenerSupport(TracingProperties tracingProperties) {
    this.correlationFields = tracingProperties.getBaggage().getCorrelation().getFields();
  }

  @Override
  public void onEvent(Object event) {
    switch (event) {
      case
        EventPublishingContextWrapper.ScopeAttachedEvent scopeAttachedEvent when scopeAttachedEvent.getSpan() == null ->
        setContext();
      case
        EventPublishingContextWrapper.ScopeRestoredEvent scopeRestoredEvent when scopeRestoredEvent.getSpan() == null ->
        setContext();
      default -> {
        // Closing event or event having Span is handled by Slf4JEventListener
      }
    }
  }

  private void setContext() {
    Span currentSpan = SpanShimHolder.getCurrentSpan();
    if (currentSpan != null) {
      SpanContext currentSpanContext = currentSpan.context();

      MDC.put("traceId", currentSpanContext.toTraceId());
      MDC.put("spanId", currentSpanContext.toSpanId());

      currentSpanContext.baggageItems().forEach(i -> {
        if (correlationFields.contains(i.getKey())) {
          MDC.put(i.getKey(), i.getValue());
        }
      });

    }
  }
}
