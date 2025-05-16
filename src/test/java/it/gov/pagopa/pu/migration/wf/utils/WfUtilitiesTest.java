package it.gov.pagopa.pu.migration.wf.utils;

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
}
