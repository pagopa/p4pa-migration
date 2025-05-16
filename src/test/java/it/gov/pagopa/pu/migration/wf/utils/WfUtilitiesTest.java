package it.gov.pagopa.pu.migration.wf.utils;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WfUtilitiesTest {

  @Test
  void whenGenerateWorkflowIdThenOk(){
    String workflowId = WfUtilities.generateWorkflowId(1L, WfUtilities.class);

    assertEquals("WfUtilities-1", workflowId);
  }

  @Test
  void givenGenerateWorkflowIdWhenIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, WfUtilities.class);
  }

  @Test
  void givenGenerateWorkflowIdWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(1L, null);
  }

  private static void testGenerateWorkflowIdWhenNullErrors(Long id, Class<?> workflow) {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> WfUtilities.generateWorkflowId(id, workflow)
    );

    assertEquals("The ID or the workflow must not be null", exception.getMessage());
  }

  @Test
  void givenNormalExceptionWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    String expectedResult = "DUMMY";
    RuntimeException exception = new RuntimeException(expectedResult);

    // When
    String result = WfUtilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenActivityExceptionHavingNormalExceptionWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    RuntimeException cause = new RuntimeException("DUMMY");
    RuntimeException exception = new ActivityFailure("X", 0, 0, "", "", null, "", cause);

    // When
    String result = WfUtilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals("Activity with activityType='' failed: 'X'. scheduledEventId=0, startedEventId=0, activityId=, identity='', retryState=null", result);
  }

  @Test
  void givenActivityExceptionHavingApplicationFailureWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    RuntimeException exception = new ActivityFailure("X", 0, 0, "", "", null, "", ApplicationFailure.newFailure("DUMMY","Y"));

    // When
    String result = WfUtilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals("DUMMY", result);
  }
}
