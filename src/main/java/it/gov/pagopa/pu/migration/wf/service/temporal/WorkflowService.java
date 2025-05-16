package it.gov.pagopa.pu.migration.wf.service.temporal;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface WorkflowService {

  <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId);

  WorkflowStub buildUntypedWorkflowStub(String taskQueue, String workflowId);

  WorkflowStatusDTO getWorkflowStatus(String workflowId);

  <T> T buildWorkflowStubDelayed(Class<T> workflowClass, String taskQueue, String workflowId, Duration startDelay);

  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDate date);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, LocalDateTime dateTime);
  <T> T buildWorkflowStubScheduled(Class<T> workflowClass, String taskQueue, String workflowId, OffsetDateTime dateTime);

  void  cancelWorkflow(String workflowId);
}
