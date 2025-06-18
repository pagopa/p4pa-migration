package it.gov.pagopa.pu.migration.wf.client.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.utils.WfUtilities;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentnotification.PaymentNotificationDataMigrationWF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationDataMigrationWFClient implements DataMigrationWfClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public WorkflowCreatedDTO migrate(Long uploadId) {
    log.info("Starting payment notification data migration having id {}", uploadId);
    String taskQueue = WfConstants.TASK_QUEUE_MIGRATION;
    String workflowId = WfUtilities.generateWorkflowId(uploadId, PaymentNotificationDataMigrationWF.class);

    PaymentNotificationDataMigrationWF workflow = workflowService.buildWorkflowStub(
      PaymentNotificationDataMigrationWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::migrate, uploadId);
  }
}
