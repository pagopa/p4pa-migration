package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessmentsregistry;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AssessmentsRegistryDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
