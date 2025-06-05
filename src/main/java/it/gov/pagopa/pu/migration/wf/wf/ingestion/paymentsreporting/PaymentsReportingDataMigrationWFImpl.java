package it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentsreporting;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentsreporting.PaymentsReportingMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class PaymentsReportingDataMigrationWFImpl extends BaseDataMigrationWFImpl implements PaymentsReportingDataMigrationWF {

  private PaymentsReportingMigrationFileTypeHandlerActivity paymentsReportingMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    paymentsReportingMigrationFileTypeHandlerActivity = wfConfig.buildPaymentsReportingMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected PaymentsReportingMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return paymentsReportingMigrationFileTypeHandlerActivity;
  }
}
