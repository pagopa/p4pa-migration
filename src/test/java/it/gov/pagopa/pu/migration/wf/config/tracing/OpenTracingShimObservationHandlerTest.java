package it.gov.pagopa.pu.migration.wf.config.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelTraceContextBuilder;
import io.opentelemetry.opentracingshim.SpanShimHolder;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class OpenTracingShimObservationHandlerTest {

  @Mock
  private TracingProperties tracingPropertiesMock;
  @Mock
  private Tracer tracerMock;

  private OpenTracingShimObservationHandler handler;

  @BeforeEach
  void init() {
    TracingProperties.Baggage baggageMock = Mockito.mock(TracingProperties.Baggage.class);
    TracingProperties.Baggage.Correlation correlationMock = Mockito.mock(TracingProperties.Baggage.Correlation.class);

    Mockito.when(tracingPropertiesMock.getBaggage())
      .thenReturn(baggageMock);
    Mockito.when(baggageMock.getCorrelation())
      .thenReturn(correlationMock);
    Mockito.when(correlationMock.getFields())
      .thenReturn(List.of("propagatedField"));

    handler = new OpenTracingShimObservationHandler(tracingPropertiesMock, tracerMock);
  }

  @Test
  void givenNoShimSpanWhenSupportsContextThenDoNothing() {
    // Given no context configured

    // When
    boolean result = handler.supportsContext(null);

    // Then
    Assertions.assertFalse(result);
  }

  @Test
  void givenMicrometerSpanWhenSupportsContextThenDoNothing() {
    try (MockedStatic<SpanShimHolder> spanShimHolderMock = Mockito.mockStatic(SpanShimHolder.class)) {
      // Given
      Span shimSpanMock = Mockito.mock(Span.class);
      io.micrometer.tracing.Span micrometerSpanMock = Mockito.mock(io.micrometer.tracing.Span.class);

      spanShimHolderMock.when(SpanShimHolder::getCurrentSpan)
        .thenReturn(shimSpanMock);
      Mockito.when(tracerMock.currentSpan())
        .thenReturn(micrometerSpanMock);

      // When
      boolean result = handler.supportsContext(null);

      // Then
      Assertions.assertFalse(result);
    }
  }

  @Test
  void givenShimSpanAndNoMicrometerSpanWhenSupportsContextThenConfigureContext() {
    try (MockedStatic<SpanShimHolder> spanShimHolderMock = Mockito.mockStatic(SpanShimHolder.class)) {
      // Given
      Span shimSpanMock = Mockito.mock(Span.class);
      SpanContext shimSpanContextMock = Mockito.mock(SpanContext.class);
      Observation.Context observationContextMock = Mockito.mock(Observation.Context.class);
      io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock = Mockito.mock(io.micrometer.tracing.Span.Builder.class);

      io.micrometer.tracing.Span injectedMicrometerSpan = Mockito.mock(io.micrometer.tracing.Span.class);

      spanShimHolderMock.when(SpanShimHolder::getCurrentSpan)
        .thenReturn(shimSpanMock);
      Mockito.when(tracerMock.currentSpan())
        .thenReturn(null);

      Mockito.when(shimSpanMock.context())
        .thenReturn(shimSpanContextMock);
      Mockito.when(shimSpanContextMock.toTraceId())
        .thenReturn("TRACEID");
      Mockito.when(shimSpanContextMock.toSpanId())
        .thenReturn("SPANID");
      Mockito.when(shimSpanContextMock.baggageItems())
        .thenReturn(List.of(
          Pair.of("propagatedField", "propagatedValue"),
          Pair.of("nonPropagatedField", "nonPropagatedValue")
        ));

      Mockito.when(tracerMock.spanBuilder())
        .thenReturn(micrometerSpanBuilderMock);
      Mockito.when(micrometerSpanBuilderMock.setParent(
          new OtelTraceContextBuilder()
            .parentId("TRACEID")
            .traceId("TRACEID")
            .spanId("SPANID")
            .sampled(false)
            .build()
        ))
        .thenReturn(micrometerSpanBuilderMock);

      Mockito.when(micrometerSpanBuilderMock.tag("propagatedField", "propagatedValue"))
        .thenReturn(micrometerSpanBuilderMock);
      Mockito.when(micrometerSpanBuilderMock.start())
        .thenReturn(injectedMicrometerSpan);

      // When
      boolean result = handler.supportsContext(observationContextMock);

      // Then
      Assertions.assertFalse(result);

      Mockito.verify(observationContextMock)
        .get(TracingObservationHandler.TracingContext.class);
      Mockito.verify(observationContextMock)
        .put(Mockito.eq(TracingObservationHandler.TracingContext.class), Mockito.<TracingObservationHandler.TracingContext>argThat(context ->
          injectedMicrometerSpan == context.getSpan()));

      Mockito.verifyNoMoreInteractions(
        shimSpanContextMock,
        micrometerSpanBuilderMock,
        observationContextMock
      );
    }
  }
}
