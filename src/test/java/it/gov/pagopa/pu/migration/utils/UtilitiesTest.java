package it.gov.pagopa.pu.migration.utils;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.OffsetDateTime;

public class UtilitiesTest {

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }

  @Test
  void givenEmptyTimeStampWhenProtobufTimestamp2OffsetDateTimeThenNull(){
    Assertions.assertNull(Utilities.protobufTimestamp2OffsetDateTime(Timestamp.getDefaultInstance()));
  }

  @Test
  void whenProtobufTimestamp2OffsetDateTimeThenReturnConversion(){
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Timestamp ts = Timestamp.getDefaultInstance().toBuilder()
      .setSeconds(now.toEpochSecond())
      .setNanos(now.getNano())
      .build();

    // When
    OffsetDateTime result = Utilities.protobufTimestamp2OffsetDateTime(ts);

    // Then
    Assertions.assertEquals(now, result);
  }

  @Test
  void whenProtobufDuration2DurationThenReturnConversion(){
    // Given
    Duration expectedResult = Duration.ofMillis(537L);
    com.google.protobuf.Duration d = com.google.protobuf.Duration.getDefaultInstance().toBuilder()
      .setSeconds(expectedResult.getSeconds())
      .setNanos(expectedResult.getNano())
      .build();

    // When
    Duration result = Utilities.protobufDuration2Duration(d);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
