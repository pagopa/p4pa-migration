package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontypeorg;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DebtPositionTypeOrgDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
