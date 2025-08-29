package it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentnotification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentnotification.PaymentNotificationMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class PaymentNotificationDataMigrationWFImpl extends BaseDataMigrationWFImpl implements PaymentNotificationDataMigrationWF {

  private PaymentNotificationMigrationFileTypeHandlerActivity paymentNotificationMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    paymentNotificationMigrationFileTypeHandlerActivity = wfConfig.buildPaymentNotificationMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected PaymentNotificationMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return paymentNotificationMigrationFileTypeHandlerActivity;
  }
}
