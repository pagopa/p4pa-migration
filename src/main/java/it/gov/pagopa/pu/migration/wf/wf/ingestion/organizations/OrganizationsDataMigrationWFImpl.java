package it.gov.pagopa.pu.migration.wf.wf.ingestion.organizations;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@WorkflowImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class OrganizationsDataMigrationWFImpl extends BaseDataMigrationWFImpl implements OrganizationsDataMigrationWF {

  private OrganizationsMigrationFileTypeHandlerActivity organizationsMigrationFileTypeHandlerActivity;

  @Override
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
    organizationsMigrationFileTypeHandlerActivity = wfConfig.buildOrganizationMigrationFileTypeHandlerActivityStub();
  }

  @Override
  protected OrganizationsMigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity() {
    return organizationsMigrationFileTypeHandlerActivity;
  }
}
