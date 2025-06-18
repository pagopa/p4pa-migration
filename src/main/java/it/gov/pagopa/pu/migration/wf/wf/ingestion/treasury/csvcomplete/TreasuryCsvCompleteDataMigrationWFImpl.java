package it.gov.pagopa.pu.migration.wf.wf.ingestion.treasury.csvcomplete;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.treasury.csvcomplete.TreasuryCsvCompleteFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class TreasuryCsvCompleteDataMigrationWFImpl extends BaseDataMigrationWFImpl implements TreasuryCsvCompleteDataMigrationWF {

  private TreasuryCsvCompleteFileTypeHandlerActivity treasuryMigrationMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    treasuryMigrationMigrationFileTypeHandlerActivity = wfConfig.buildTreasuryCsvCompleteMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected TreasuryCsvCompleteFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return treasuryMigrationMigrationFileTypeHandlerActivity;
  }
}
