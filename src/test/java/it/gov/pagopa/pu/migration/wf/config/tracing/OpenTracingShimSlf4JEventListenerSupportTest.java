package it.gov.pagopa.pu.migration.wf.config.tracing;

import io.micrometer.tracing.otel.bridge.EventPublishingContextWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.opentracingshim.SpanShimHolder;
import io.opentracing.SpanContext;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.MDC;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class OpenTracingShimSlf4JEventListenerSupportTest {

  @Mock
  private TracingProperties tracingPropertiesMock;

  private OpenTracingShimSlf4jEventListenerSupport openTracingShimSlf4JEventListenerSupport;

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

    openTracingShimSlf4JEventListenerSupport = new OpenTracingShimSlf4jEventListenerSupport(tracingPropertiesMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(tracingPropertiesMock);
  }

  @Test
  void givenClosedEventWhenOnEventThenDoNothing() {
    try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
      // Given
      EventPublishingContextWrapper.ScopeClosedEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeClosedEvent.class);

      // When
      openTracingShimSlf4JEventListenerSupport.onEvent(event);

      // Then
      mdcMock.verifyNoInteractions();
    }
  }

  @Test
  void givenOtelSpanAndAttachedEventWhenOnEventThenDoNothing() {
    EventPublishingContextWrapper.ScopeAttachedEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeAttachedEvent.class);
    givenOtelSpanWhenOnEventThenDoNothing(event, Mockito.when(event.getSpan()));
  }

  @Test
  void givenOtelSpanAndRestoredEventWhenOnEventThenDoNothing() {
    EventPublishingContextWrapper.ScopeRestoredEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeRestoredEvent.class);
    givenOtelSpanWhenOnEventThenDoNothing(event, Mockito.when(event.getSpan()));
  }

  void givenOtelSpanWhenOnEventThenDoNothing(Object eventMock, OngoingStubbing<Span> getSpanStub) {
    try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
      getSpanStub
        .thenReturn(Mockito.mock(Span.class));

      openTracingShimSlf4JEventListenerSupport.onEvent(eventMock);

      mdcMock.verifyNoInteractions();
    }
  }

  @Test
  void givenNoOtelSpanAndNoShimSpanAndAttachedEventWhenOnEventThenDoNothing() {
    EventPublishingContextWrapper.ScopeAttachedEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeAttachedEvent.class);
    givenNoOtelSpanAndNoShimSpanWhenOnEventThenDoNothing(event, Mockito.when(event.getSpan()));
  }

  @Test
  void givenNoOtelSpanAndNoShimSpanAndRestoredEventWhenOnEventThenDoNothing() {
    EventPublishingContextWrapper.ScopeRestoredEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeRestoredEvent.class);
    givenNoOtelSpanAndNoShimSpanWhenOnEventThenDoNothing(event, Mockito.when(event.getSpan()));
  }

  void givenNoOtelSpanAndNoShimSpanWhenOnEventThenDoNothing(Object eventMock, OngoingStubbing<Span> getSpanStub) {
    try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
      getSpanStub
        .thenReturn(null);

      openTracingShimSlf4JEventListenerSupport.onEvent(eventMock);

      mdcMock.verifyNoInteractions();
    }
  }

  @Test
  void givenNoOtelSpanAndShinSpanAndAttachEventWhenOnEventThenConfigureSlf4jContext() {
    EventPublishingContextWrapper.ScopeAttachedEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeAttachedEvent.class);
    givenNoOtelSpanAndShinSpanWhenOnEventThenConfigureSlf4jContext(event, Mockito.when(event.getSpan()));
  }

  @Test
  void givenNoOtelSpanAndShimSpanAndRestoredEventWhenOnEventThenDoNothing() {
    EventPublishingContextWrapper.ScopeRestoredEvent event = Mockito.mock(EventPublishingContextWrapper.ScopeRestoredEvent.class);
    givenNoOtelSpanAndShinSpanWhenOnEventThenConfigureSlf4jContext(event, Mockito.when(event.getSpan()));
  }

  void givenNoOtelSpanAndShinSpanWhenOnEventThenConfigureSlf4jContext(Object eventMock, OngoingStubbing<Span> getSpanStub) {
    try (
      MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class);
      MockedStatic<SpanShimHolder> spanShimHolderMock = Mockito.mockStatic(SpanShimHolder.class)
    ) {
      io.opentracing.Span spanShimMock = Mockito.mock(io.opentracing.Span.class);
      SpanContext spanShimContextMock = Mockito.mock(SpanContext.class);

      getSpanStub
        .thenReturn(null);
      spanShimHolderMock.when(SpanShimHolder::getCurrentSpan)
        .thenReturn(spanShimMock);
      Mockito.when(spanShimMock.context())
        .thenReturn(spanShimContextMock);

      Mockito.when(spanShimContextMock.toTraceId())
        .thenReturn("TRACEID");
      Mockito.when(spanShimContextMock.toSpanId())
        .thenReturn("SPANID");
      Mockito.when(spanShimContextMock.baggageItems())
        .thenReturn(List.of(
          Pair.of("propagatedField", "propagatedValue"),
          Pair.of("nonPropagatedField", "nonPropagatedValue")
        ));

      openTracingShimSlf4JEventListenerSupport.onEvent(eventMock);

      mdcMock.verify(() -> MDC.put("traceId", "TRACEID"));
      mdcMock.verify(() -> MDC.put("spanId", "SPANID"));
      mdcMock.verify(() -> MDC.put("propagatedField", "propagatedValue"));

      mdcMock.verifyNoMoreInteractions();
    }
  }
}
