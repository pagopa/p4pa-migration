package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositions;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DebtPositionsDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
