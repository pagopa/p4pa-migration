package it.gov.pagopa.pu.migration.wf.service.temporal;

import com.uber.m3.tally.NoopScope;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.failure.TemporalException;
import io.temporal.internal.client.WorkflowClientHelper;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.migration.exception.WorkflowNotFoundException;
import it.gov.pagopa.pu.migration.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

  private final String namespace;
  private final WorkflowClient workflowClient;

  public WorkflowServiceImpl(
    @Value("${spring.temporal.namespace}") String namespace,
    WorkflowClient workflowClient) {
    this.namespace = namespace;
    this.workflowClient = workflowClient;
  }

  @Override
  public <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId) {
    return workflowClient.newWorkflowStub(
      workflowClass,
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .build());
  }

  @Override
  public WorkflowStub  buildUntypedWorkflowStub( String taskQueue, String workflowId) {
    return workflowClient.newUntypedWorkflowStub(
      taskQueue,
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .build());
  }

  @Override
  public WorkflowStatusDTO getWorkflowStatus(String workflowId) {
    try {
      log.debug("Retrieving workflow status for workflowId: {}", workflowId);
      WorkflowExecutionInfo info = WorkflowClientHelper.describeWorkflowInstance(
        workflowClient.getWorkflowServiceStubs(),
        namespace,
        WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(),
        new NoopScope()
      );

      return WorkflowStatusDTO.builder()
        .workflowId(workflowId)
        .workflowType(info.getType().getName())
        .runId(info.getExecution().getRunId())
        .taskQueue(info.getTaskQueue())
        .status(info.getStatus().name())
        .startDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getStartTime()))
        .executionDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getExecutionTime()))
        .endDateTime(Utilities.protobufTimestamp2OffsetDateTime(info.getCloseTime()))
        .duration(Utilities.protobufDuration2Duration(info.getExecutionDuration()).toString())
        .build();

    } catch (StatusRuntimeException e) {
      if(Status.NOT_FOUND.getCode().equals(e.getStatus().getCode())) {
        log.error("Workflow with ID {} not found", workflowId);
        throw new WorkflowNotFoundException(e.getMessage());
      }
      log.error("An error occurred while retrieving the workflow status: {}", e.getMessage());
      throw new IllegalStateException(e.getMessage());
    } catch (io.temporal.client.WorkflowNotFoundException e) {
      log.error("Workflow with ID {} not found", workflowId);
      throw new WorkflowNotFoundException(e.getMessage());
    } catch (TemporalException e) {
      log.error("An error occurred while retrieving the workflow status: {}", e.getMessage());
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  /** This method should be called in all workflows having signalMethods before to handle signal outcome */
  public static void waitForSignalMethods(){
    log.info("Waiting for signal handlers");
    Workflow.await(Workflow::isEveryHandlerFinished);
    log.info("All pending signals have been handled");
  }

  @Override
  public <T> T buildWorkflowStubDelayed(Class<T> workflowClass, String taskQueue, String workflowId, Duration startDelay) {
    if(startDelay.isNegative()){
      startDelay = Duration.ZERO;
    }
    return workflowClient.newWorkflowStub(
      workflowClass,
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .setStartDelay(startDelay)
        .build());
  }

  @Override
  public <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDate date) {
    return buildWorkflowStubScheduled(workflowClass, taskQueue, workflowId, LocalDateTime.of(date, LocalTime.MIDNIGHT));
  }

  @Override
  public <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDateTime dateTime) {
    Duration startDelay = Duration.between(LocalDateTime.now(), dateTime);
    return buildWorkflowStubDelayed(workflowClass, taskQueue, workflowId, startDelay);
  }

  @Override
  public <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, OffsetDateTime dateTime) {
    Duration startDelay = Duration.between(OffsetDateTime.now(), dateTime);
    return buildWorkflowStubDelayed(workflowClass, taskQueue, workflowId, startDelay);
  }

  @Override
  public void  cancelWorkflow(String workflowId) {
    try {
      workflowClient.newUntypedWorkflowStub(workflowId)
        .cancel();
    } catch (io.temporal.client.WorkflowNotFoundException e) {
      // Nothing to do
    }
  }
}
