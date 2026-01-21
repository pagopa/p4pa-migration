package io.opentelemetry.opentracingshim;

import io.opentracing.Span;

public class SpanShimHolder {

  private SpanShimHolder(){}

  public static Span getCurrentSpan(){
    return SpanShim.current();
  }
}
