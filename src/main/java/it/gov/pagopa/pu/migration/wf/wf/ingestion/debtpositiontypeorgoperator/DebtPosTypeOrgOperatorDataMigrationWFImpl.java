package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontypeorgoperator;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator.DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class DebtPosTypeOrgOperatorDataMigrationWFImpl extends BaseDataMigrationWFImpl implements DebtPosTypeOrgOperatorDataMigrationWF {

  private DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity debtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    debtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity = wfConfig.buildDebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return debtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity;
  }
}
