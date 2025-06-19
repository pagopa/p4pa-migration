package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessmentsregistry;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessmentsregistry.AssessmentsRegistryMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class AssessmentsRegistryDataMigrationWFImpl extends BaseDataMigrationWFImpl implements AssessmentsRegistryDataMigrationWF {

  private AssessmentsRegistryMigrationFileTypeHandlerActivity assessmentsRegistryMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    assessmentsRegistryMigrationFileTypeHandlerActivity = wfConfig.buildAssessmentsRegistryMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected AssessmentsRegistryMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return assessmentsRegistryMigrationFileTypeHandlerActivity;
  }
}
