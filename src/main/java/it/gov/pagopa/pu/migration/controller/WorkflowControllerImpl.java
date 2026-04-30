package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.controller.generated.WorkflowApi;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WorkflowControllerImpl implements WorkflowApi {

  private final WorkflowService workflowService;

  public WorkflowControllerImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public ResponseEntity<WorkflowStatusDTO> getWorkflowStatus(String workflowId) {
    log.info("Retrieving Workflow status of {}", workflowId);
    return ResponseEntity.ok(workflowService.getWorkflowStatus(workflowId));
  }
}
