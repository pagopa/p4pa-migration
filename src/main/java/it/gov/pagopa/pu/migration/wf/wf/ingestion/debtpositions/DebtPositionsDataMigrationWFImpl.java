package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositions;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositions.DebtPositionsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class DebtPositionsDataMigrationWFImpl extends BaseDataMigrationWFImpl implements DebtPositionsDataMigrationWF {

  private DebtPositionsMigrationFileTypeHandlerActivity debtPositionsMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    debtPositionsMigrationFileTypeHandlerActivity = wfConfig.buildDebtPositionsMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected DebtPositionsMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return debtPositionsMigrationFileTypeHandlerActivity;
  }
}
