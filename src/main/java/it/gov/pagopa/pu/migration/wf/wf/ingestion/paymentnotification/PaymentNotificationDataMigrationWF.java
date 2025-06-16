package it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
@WorkflowInterface
public interface PaymentNotificationDataMigrationWF {

  @WorkflowMethod
  void migrate(long uploadId);
}
