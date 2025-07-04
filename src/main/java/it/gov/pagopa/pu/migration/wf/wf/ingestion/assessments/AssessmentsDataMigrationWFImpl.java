package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessments;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessments.AssessmentsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class AssessmentsDataMigrationWFImpl extends BaseDataMigrationWFImpl implements AssessmentsDataMigrationWF {

  private AssessmentsMigrationFileTypeHandlerActivity assessmentsMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    assessmentsMigrationFileTypeHandlerActivity = wfConfig.buildAssessmentsMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected AssessmentsMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return assessmentsMigrationFileTypeHandlerActivity;
  }
}
