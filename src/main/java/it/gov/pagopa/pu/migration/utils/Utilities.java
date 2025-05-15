package it.gov.pagopa.pu.migration.utils;

import org.slf4j.MDC;

public class Utilities {
  private Utilities(){}

  public static String getTraceId(){
    return MDC.get("traceId");
  }
}
