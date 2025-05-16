package it.gov.pagopa.pu.migration.wf.wf.ingestion.organizations;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrganizationsDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
