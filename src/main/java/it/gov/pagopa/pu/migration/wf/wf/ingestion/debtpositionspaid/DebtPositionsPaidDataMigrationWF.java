package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositionspaid;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DebtPositionsPaidDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
