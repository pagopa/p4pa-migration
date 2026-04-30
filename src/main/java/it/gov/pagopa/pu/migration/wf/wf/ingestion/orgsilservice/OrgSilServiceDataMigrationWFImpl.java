package it.gov.pagopa.pu.migration.wf.wf.ingestion.orgsilservice;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.orgsilservice.OrgSilServiceMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class OrgSilServiceDataMigrationWFImpl extends BaseDataMigrationWFImpl implements OrgSilServiceDataMigrationWF {

  private OrgSilServiceMigrationFileTypeHandlerActivity orgSilServiceMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    orgSilServiceMigrationFileTypeHandlerActivity = wfConfig.buildOrgSilServiceMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected OrgSilServiceMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return orgSilServiceMigrationFileTypeHandlerActivity;
  }
}
