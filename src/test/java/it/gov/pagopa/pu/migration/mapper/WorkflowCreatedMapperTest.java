package it.gov.pagopa.pu.migration.mapper;

import io.temporal.api.common.v1.WorkflowExecution;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowCreatedMapperTest {

  @Test
  void test(){
    // Given
    String workflowId = "workflowId";
    String runId = "runId";
    WorkflowExecution wfExecMock = Mockito.mock(WorkflowExecution.class);

    Mockito.when(wfExecMock.getWorkflowId())
      .thenReturn(workflowId);
    Mockito.when(wfExecMock.getRunId())
      .thenReturn(runId);

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(workflowId, runId);

    // When
    WorkflowCreatedDTO result = WorkflowCreatedMapper.map(wfExecMock);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
