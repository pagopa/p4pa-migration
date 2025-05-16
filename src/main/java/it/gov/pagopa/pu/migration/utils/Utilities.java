package it.gov.pagopa.pu.migration.utils;

import com.google.protobuf.Timestamp;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class Utilities {
  private Utilities(){}

  public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");

  public static String getTraceId(){
    return MDC.get("traceId");
  }

  public static OffsetDateTime protobufTimestamp2OffsetDateTime(Timestamp ts){
    if(ts.getSeconds()>0) {
      return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).atZone(ZONEID).toOffsetDateTime();
    } else{
      return null;
    }
  }

  public static Duration protobufDuration2Duration(com.google.protobuf.Duration d) {
    return Duration.ofSeconds(d.getSeconds(), d.getNanos());
  }
}
