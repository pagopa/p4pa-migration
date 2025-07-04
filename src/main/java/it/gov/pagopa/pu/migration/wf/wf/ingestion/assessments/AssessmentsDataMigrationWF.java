package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AssessmentsDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
