package it.gov.pagopa.pu.migration.wf.service.temporal;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.utils.TemporalTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
class WorkflowClientServiceTest {

  private final WorkflowClientService service = new WorkflowClientServiceImpl();

  @Test
  void testStartWfProc1(){
    try(MockedStatic<WorkflowClient> workflowClientMockedStatic = Mockito.mockStatic(WorkflowClient.class)){
      // Given
      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
      WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);

      Functions.Proc1<Object> wf = Mockito.mock(Functions.Proc1.class);
      Object arg1 = new Object();

      workflowClientMockedStatic.when(() -> WorkflowClient.start(Mockito.same(wf),
          Mockito.same(arg1)))
        .thenReturn(wfExecMock);

      // When
      WorkflowCreatedDTO result = service.start(wf,
        arg1);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void testStartWfProc2(){
    try(MockedStatic<WorkflowClient> workflowClientMockedStatic = Mockito.mockStatic(WorkflowClient.class)){
      // Given
      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
      WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);

      Functions.Proc2<Object, Object> wf = Mockito.mock(Functions.Proc2.class);
      Object arg1 = new Object();
      Object arg2 = new Object();

      workflowClientMockedStatic.when(() -> WorkflowClient.start(Mockito.same(wf),
          Mockito.same(arg1), Mockito.same(arg2)))
        .thenReturn(wfExecMock);

      // When
      WorkflowCreatedDTO result = service.start(wf,
        arg1, arg2);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void testStartWfProc3(){
    try(MockedStatic<WorkflowClient> workflowClientMockedStatic = Mockito.mockStatic(WorkflowClient.class)){
      // Given
      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
      WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);

      Functions.Proc3<Object, Object, Object> wf = Mockito.mock(Functions.Proc3.class);
      Object arg1 = new Object();
      Object arg2 = new Object();
      Object arg3 = new Object();

      workflowClientMockedStatic.when(() -> WorkflowClient.start(Mockito.same(wf),
          Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
        .thenReturn(wfExecMock);

      // When
      WorkflowCreatedDTO result = service.start(wf,
        arg1, arg2, arg3);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void testStartWfProc4(){
    try(MockedStatic<WorkflowClient> workflowClientMockedStatic = Mockito.mockStatic(WorkflowClient.class)){
      // Given
      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
      WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);

      Functions.Proc4<Object, Object, Object, Object> wf = Mockito.mock(Functions.Proc4.class);
      Object arg1 = new Object();
      Object arg2 = new Object();
      Object arg3 = new Object();
      Object arg4 = new Object();

      workflowClientMockedStatic.when(() -> WorkflowClient.start(Mockito.same(wf),
          Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4)))
        .thenReturn(wfExecMock);

      // When
      WorkflowCreatedDTO result = service.start(wf,
        arg1, arg2, arg3, arg4);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void testStartWfProc5(){
    try(MockedStatic<WorkflowClient> workflowClientMockedStatic = Mockito.mockStatic(WorkflowClient.class)){
      // Given
      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
      WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);

      Functions.Proc5<Object, Object, Object, Object, Object> wf = Mockito.mock(Functions.Proc5.class);
      Object arg1 = new Object();
      Object arg2 = new Object();
      Object arg3 = new Object();
      Object arg4 = new Object();
      Object arg5 = new Object();

      workflowClientMockedStatic.when(() -> WorkflowClient.start(Mockito.same(wf),
          Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4), Mockito.same(arg5)))
        .thenReturn(wfExecMock);

      // When
      WorkflowCreatedDTO result = service.start(wf,
        arg1, arg2, arg3, arg4, arg5);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void testSignalWithStart(){
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    WorkflowExecution wfExecMock = TemporalTestUtils.buildWorkflowExecutionMock(expectedResult);
    WorkflowStub workflowStub = Mockito.mock(WorkflowStub.class);
    String signalMethod = "signalMethod";
    Object[] signalArgs = new Object[0];
    Object[] startArgs = new Object[0];

    Mockito.when(workflowStub.signalWithStart(Mockito.same(signalMethod), Mockito.same(signalArgs), Mockito.same(startArgs)))
      .thenReturn(wfExecMock);

    // When
    WorkflowCreatedDTO result = service.signalWithStart(workflowStub, signalMethod, signalArgs, startArgs);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
