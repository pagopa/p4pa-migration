package it.gov.pagopa.pu.migration.wf.service.temporal;

import com.google.protobuf.Timestamp;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.common.v1.WorkflowType;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowServiceException;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.client.WorkflowClientHelper;
import io.temporal.serviceclient.WorkflowServiceStubs;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.migration.exception.WorkflowNotFoundException;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.migration.utils.Utilities;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.organizations.OrganizationsDataMigrationWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

  @Mock
  private WorkflowClient workflowClientMock;
  @Mock
  private OrganizationsDataMigrationWF wfMock;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private WorkflowExecutionInfo workflowExecutionInfoMock;
  @Mock
  private WorkflowServiceStubs workflowServiceStubsMock;

  private final String namespace = "NAMESPACE";
  private WorkflowService workflowService;

  @BeforeEach
  void init() {
    workflowService = Mockito.spy(new WorkflowServiceImpl(namespace, workflowClientMock));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowClientMock, wfMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String workflowId = String.valueOf(ingestionFlowFileId);

    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(OrganizationsDataMigrationWF.class),
      Mockito.<WorkflowOptions>argThat(options ->
        WfConstants.TASK_QUEUE_MIGRATION.equals(options.getTaskQueue()) &&
          workflowId.equals(options.getWorkflowId())
      )))
      .thenReturn(wfMock);

    // When
    OrganizationsDataMigrationWF result = workflowService.buildWorkflowStub(OrganizationsDataMigrationWF.class, WfConstants.TASK_QUEUE_MIGRATION, workflowId);

    // Then
    Assertions.assertSame(wfMock, result);
  }

  @Test
  void givenGetWorkflowStatusThenSuccess() {
    // Given
    String workflowId = "test-workflow-id";
    WorkflowStatusDTO expectedResult = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .workflowType("WFTYPE")
      .runId("RUNID")
      .taskQueue("TASKQUEUE")
      .startDateTime(OffsetDateTime.now(Utilities.ZONEID))
      .executionDateTime(OffsetDateTime.now(Utilities.ZONEID).plusMinutes(1))
      .endDateTime(OffsetDateTime.now(Utilities.ZONEID).plusDays(1))
      .duration("PT0S")
      .status(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING.name())
      .build();

    when(workflowExecutionInfoMock.getType()).thenReturn(WorkflowType.newBuilder().setName(expectedResult.getWorkflowType()).build());
    when(workflowExecutionInfoMock.getStatus()).thenReturn(WorkflowExecutionStatus.valueOf(expectedResult.getStatus()));
    when(workflowExecutionInfoMock.getExecution().getRunId()).thenReturn(expectedResult.getRunId());
    when(workflowExecutionInfoMock.getTaskQueue()).thenReturn(expectedResult.getTaskQueue());
    when(workflowExecutionInfoMock.getStartTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getStartDateTime())));
    when(workflowExecutionInfoMock.getExecutionTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getExecutionDateTime())));
    when(workflowExecutionInfoMock.getCloseTime()).thenReturn(offsetDateTime2ProtobufTimestamp(Objects.requireNonNull(expectedResult.getEndDateTime())));
    when(workflowExecutionInfoMock.getExecutionDuration()).thenReturn(com.google.protobuf.Duration.getDefaultInstance());

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      // When
      WorkflowStatusDTO result = workflowService.getWorkflowStatus(workflowId);

      // Then
      TestUtils.checkNotNullFields(result);
      assertEquals(expectedResult, result);
    }
  }

  private static Timestamp offsetDateTime2ProtobufTimestamp(OffsetDateTime dt) {
    return Timestamp.newBuilder().setSeconds(dt.toEpochSecond()).setNanos(dt.getNano()).build();
  }


  @Test
  void givenGetWorkflowStatusWhenWorkflowNotFoundThenThrowWorkflowNotFoundException() {
    String workflowId = "test-workflow-id";

    when(workflowExecutionInfoMock.getType()).thenThrow(new io.temporal.client.WorkflowNotFoundException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Workflow not found", null));

    when(workflowClientMock.getWorkflowServiceStubs()).thenReturn(workflowServiceStubsMock);

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      WorkflowNotFoundException exception = assertThrows(
        WorkflowNotFoundException.class,
        () -> workflowService.getWorkflowStatus(workflowId)
      );

      assertEquals("workflowId='test-workflow-id', runId='', workflowType='Workflow not found'", exception.getMessage());
    }
  }

  @Test
  void givenGetWorkflowStatusWhenInternalErrorThenThrowWorkflowInternalErrorException() {
    String workflowId = "test-workflow-id";

    when(workflowClientMock.getWorkflowServiceStubs()).thenThrow(new WorkflowServiceException(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), "Generic Error", null));

    try (MockedStatic<WorkflowClientHelper> mockedStatic = mockStatic(WorkflowClientHelper.class)) {
      mockedStatic.when(() -> WorkflowClientHelper.describeWorkflowInstance(
          any(),
          eq(namespace),
          any(),
          any()))
        .thenReturn(workflowExecutionInfoMock);

      IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> workflowService.getWorkflowStatus(workflowId)
      );

      assertEquals("workflowId='test-workflow-id', runId='', workflowType='Generic Error'", exception.getMessage());
    }
  }

  @Test
  void testBuildUntypedWorkflowStub() {
    // Given
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";
    WorkflowStub expectedStub = Mockito.mock(WorkflowStub.class);

    when(workflowClientMock.newUntypedWorkflowStub(
      eq(taskQueue),
      argThat(options -> taskQueue.equals(options.getTaskQueue()) && workflowId.equals(options.getWorkflowId()))
    )).thenReturn(expectedStub);

    // When
    WorkflowStub result = workflowService.buildUntypedWorkflowStub(taskQueue, workflowId);

    // Then
    assertEquals(expectedStub, result);
  }

  @Test
  void testBuildWorkflowDelayed() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration duration = Duration.ofDays(1);
    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(OrganizationsDataMigrationWF.class),
      Mockito.<WorkflowOptions>argThat(options -> taskQueue.equals(options.getTaskQueue()) &&
        workflowId.equals(options.getWorkflowId()) && duration.equals(options.getStartDelay()))
    )).thenReturn(wfMock);

    OrganizationsDataMigrationWF result = workflowService.buildWorkflowStubDelayed(OrganizationsDataMigrationWF.class,
      taskQueue,
      workflowId,
      duration);

    Assertions.assertSame(wfMock, result);
  }

  @Test
  void givenNegativeDurationWhenBuildWorkflowDelayedThenDurationZERO() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration expectedDuration = Duration.ZERO;
    when(workflowClientMock.newWorkflowStub(
      Mockito.eq(OrganizationsDataMigrationWF.class),
      Mockito.<WorkflowOptions>argThat(options -> taskQueue.equals(options.getTaskQueue()) &&
        workflowId.equals(options.getWorkflowId()) && expectedDuration.equals(options.getStartDelay()))
    )).thenReturn(wfMock);

    OrganizationsDataMigrationWF result = workflowService.buildWorkflowStubDelayed(OrganizationsDataMigrationWF.class,
      taskQueue,
      workflowId,
      Duration.between(LocalDateTime.now(), LocalDateTime.now().minusDays(1)));

    Assertions.assertSame(wfMock, result);
  }

  @Test
  void testBuildWorkflowScheduledWithLocalDate() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    LocalDate localDate = LocalDate.now().plusDays(1);


    OrganizationsDataMigrationWF expectedResult = mock(OrganizationsDataMigrationWF.class);
    doReturn(expectedResult)
      .when(workflowService)
      .buildWorkflowStubScheduled(
        OrganizationsDataMigrationWF.class,
        taskQueue,
        workflowId,
        LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
      );

    OrganizationsDataMigrationWF result = workflowService.buildWorkflowStubScheduled(OrganizationsDataMigrationWF.class,
      taskQueue, workflowId, localDate);

    Assertions.assertSame(expectedResult, result);

  }

  @Test
  void testBuildWorkflowScheduledWithLocalDateTime() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    LocalDateTime nextSchedule = LocalDateTime.now().plusDays(1);
    Duration expectedMaxDuration = Duration.ofDays(1);

    workflowService.buildWorkflowStubScheduled(OrganizationsDataMigrationWF.class,
      taskQueue,
      workflowId,
      nextSchedule);

    ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);

    verify(workflowClientMock).newWorkflowStub(
      eq(OrganizationsDataMigrationWF.class),
      optionsCaptor.capture()
    );

    WorkflowOptions capturedOptions = optionsCaptor.getValue();
    Duration actualStartDelay = capturedOptions.getStartDelay();
    assertNotNull(actualStartDelay);
    Duration diff = expectedMaxDuration.minus(actualStartDelay);
    assertTrue(diff.toSeconds() >= 0);
    assertTrue(diff.toSeconds() < 5);
    assertEquals(taskQueue, capturedOptions.getTaskQueue());
    assertEquals(workflowId, capturedOptions.getWorkflowId());
  }

  @Test
  void testBuildWorkflowScheduledWithOffsetDateTime() {
    String taskQueue = "test-task-queue";
    String workflowId = "test-workflow-id";

    Duration expectedMaxDuration = Duration.ofDays(1);
    OffsetDateTime nextSchedule = OffsetDateTime.now(ZoneOffset.MAX).plus(expectedMaxDuration);

    workflowService.buildWorkflowStubScheduled(OrganizationsDataMigrationWF.class,
      taskQueue,
      workflowId,
      nextSchedule);

    ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);

    verify(workflowClientMock).newWorkflowStub(
      eq(OrganizationsDataMigrationWF.class),
      optionsCaptor.capture()
    );

    WorkflowOptions capturedOptions = optionsCaptor.getValue();
    Duration actualStartDelay = capturedOptions.getStartDelay();
    assertNotNull(actualStartDelay);
    Duration diff = expectedMaxDuration.minus(actualStartDelay);
    assertTrue(diff.toSeconds() >= 0);
    assertTrue(diff.toSeconds() < 5);
    assertEquals(taskQueue, capturedOptions.getTaskQueue());
    assertEquals(workflowId, capturedOptions.getWorkflowId());
  }

  @Test
  void whenCancelWorkflowThenOk() {
    // Given
    String workflowId = "WFID";
    WorkflowStub stubMock = Mockito.mock(WorkflowStub.class);

    when(workflowClientMock.newUntypedWorkflowStub(workflowId))
      .thenReturn(stubMock);

    // When
    workflowService.cancelWorkflow(workflowId);

    // Then
    Mockito.verify(stubMock).cancel();
  }

  @Test
  void givenNotExistentWfWhenCancelWorkflowThenDoNothing() {
    // Given
    String workflowId = "WFID";

    io.temporal.client.WorkflowNotFoundException workflowNotFoundException = new io.temporal.client.WorkflowNotFoundException(mock(WorkflowExecution.class), null, null);
    when(workflowClientMock.newUntypedWorkflowStub(workflowId))
      .thenThrow(workflowNotFoundException);

    // When
    Assertions.assertDoesNotThrow(() -> workflowService.cancelWorkflow(workflowId));
  }

}
