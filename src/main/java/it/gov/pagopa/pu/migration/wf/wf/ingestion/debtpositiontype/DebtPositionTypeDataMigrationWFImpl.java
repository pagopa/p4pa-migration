package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontype;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontype.DebtPositionTypeMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class DebtPositionTypeDataMigrationWFImpl extends BaseDataMigrationWFImpl implements DebtPositionTypeDataMigrationWF {

  private DebtPositionTypeMigrationFileTypeHandlerActivity debtPositionTypeMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    debtPositionTypeMigrationFileTypeHandlerActivity = wfConfig.buildDebtPositionTypeMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected DebtPositionTypeMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return debtPositionTypeMigrationFileTypeHandlerActivity;
  }
}
