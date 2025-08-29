package it.gov.pagopa.pu.migration.wf.wf.ingestion.orgsilservice;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrgSilServiceDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
