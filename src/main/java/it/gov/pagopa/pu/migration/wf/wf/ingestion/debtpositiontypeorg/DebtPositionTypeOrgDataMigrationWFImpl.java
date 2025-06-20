package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontypeorg;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorg.DebtPositionTypeOrgMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class DebtPositionTypeOrgDataMigrationWFImpl extends BaseDataMigrationWFImpl implements DebtPositionTypeOrgDataMigrationWF {

  private DebtPositionTypeOrgMigrationFileTypeHandlerActivity debtPositionTypeOrgMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    debtPositionTypeOrgMigrationFileTypeHandlerActivity = wfConfig.buildDebtPositionTypeOrgMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected DebtPositionTypeOrgMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return debtPositionTypeOrgMigrationFileTypeHandlerActivity;
  }
}
