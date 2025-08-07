package it.gov.pagopa.pu.migration.wf.client.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.utils.WfUtilities;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositions.DebtPositionsDataMigrationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionsDataMigrationWFClient implements DataMigrationWfClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public DebtPositionsDataMigrationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO migrate(Long uploadId) {
    log.info("Starting debt positions data migration having id {}", uploadId);
    String taskQueue = WfConstants.TASK_QUEUE_MIGRATION;
    String workflowId = WfUtilities.generateWorkflowId(uploadId, DebtPositionsDataMigrationWF.class);

    DebtPositionsDataMigrationWF workflow = workflowService.buildWorkflowStub(
      DebtPositionsDataMigrationWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::migrate, uploadId);
  }
}
