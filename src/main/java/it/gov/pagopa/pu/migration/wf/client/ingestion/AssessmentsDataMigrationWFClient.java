package it.gov.pagopa.pu.migration.wf.client.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.utils.WfUtilities;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.assessments.AssessmentsDataMigrationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssessmentsDataMigrationWFClient implements DataMigrationWfClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public AssessmentsDataMigrationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO migrate(Long uploadId) {
    log.info("Starting assessments data migration having id {}", uploadId);
    String taskQueue = WfConstants.TASK_QUEUE_MIGRATION;
    String workflowId = WfUtilities.generateWorkflowId(uploadId, AssessmentsDataMigrationWF.class);

    AssessmentsDataMigrationWF workflow = workflowService.buildWorkflowStub(
      AssessmentsDataMigrationWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::migrate, uploadId);
  }
}
