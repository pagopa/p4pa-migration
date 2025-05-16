package it.gov.pagopa.pu.migration.wf.utils;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowClientService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class TemporalTestUtils {
  private TemporalTestUtils(){}

  // Helper method to check that the method exists on both classes
  public static void assertSignalMethodExists(Class<?> wfInterface, String methodName, Class<?>... parameterTypes) {
    try {
      Assertions.assertNotNull(wfInterface.getMethod(methodName, parameterTypes));
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("signalMethod doesn't exists", e);
    }
  }

  public static WorkflowExecution buildWorkflowExecutionMock(WorkflowCreatedDTO expectedResult){
    WorkflowExecution wfExecMock = Mockito.mock(WorkflowExecution.class);

    Mockito.when(wfExecMock.getWorkflowId())
      .thenReturn(expectedResult.getWorkflowId());
    Mockito.when(wfExecMock.getRunId())
      .thenReturn(expectedResult.getRunId());

    return wfExecMock;
  }

  public static <A1> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1),
        Mockito.same(arg1)))
      .thenReturn(expectedResult);
  }
  public static <A1> Functions.Proc1<A1> buildArgumentMatcherProc(A1 arg1){
    return Mockito.argThat((Functions.Proc1<A1> f) -> {
      f.apply(arg1);
      return true;
    });
  }

  public static <A1, A2> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2),
        Mockito.same(arg1), Mockito.same(arg2)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2> Functions.Proc2<A1, A2> buildArgumentMatcherProc(A1 arg1, A2 arg2){
    return Mockito.argThat((Functions.Proc2<A1, A2> f) -> {
      f.apply(arg1, arg2);
      return true;
    });
  }

  public static <A1, A2, A3> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3> Functions.Proc3<A1, A2, A3> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3){
    return Mockito.argThat((Functions.Proc3<A1, A2, A3> f) -> {
      f.apply(arg1, arg2, arg3);
      return true;
    });
  }

  public static <A1, A2, A3, A4> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3, A4 arg4){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3, arg4),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3, A4> Functions.Proc4<A1, A2, A3, A4> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3, A4 arg4){
    return Mockito.argThat((Functions.Proc4<A1, A2, A3, A4> f) -> {
      f.apply(arg1, arg2, arg3, arg4);
      return true;
    });
  }

  public static <A1, A2, A3, A4, A5> void configureWorkflowClientServiceMock(WorkflowClientService workflowClientServiceMock, WorkflowCreatedDTO expectedResult, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5){
    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(arg1, arg2, arg3, arg4, arg5),
        Mockito.same(arg1), Mockito.same(arg2), Mockito.same(arg3), Mockito.same(arg4), Mockito.same(arg5)))
      .thenReturn(expectedResult);
  }
  public static <A1, A2, A3, A4, A5> Functions.Proc5<A1, A2, A3, A4, A5> buildArgumentMatcherProc(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5){
    return Mockito.argThat((Functions.Proc5<A1, A2, A3, A4, A5> f) -> {
      f.apply(arg1, arg2, arg3, arg4, arg5);
      return true;
    });
  }
}
