package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositionspaid;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositionspaid.DebtPositionsPaidMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class DebtPositionsPaidDataMigrationWFImpl extends BaseDataMigrationWFImpl implements DebtPositionsPaidDataMigrationWF {

  private DebtPositionsPaidMigrationFileTypeHandlerActivity debtPositionsPaidMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    debtPositionsPaidMigrationFileTypeHandlerActivity = wfConfig.buildDebtPositionsPaidMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected DebtPositionsPaidMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return debtPositionsPaidMigrationFileTypeHandlerActivity;
  }
}
