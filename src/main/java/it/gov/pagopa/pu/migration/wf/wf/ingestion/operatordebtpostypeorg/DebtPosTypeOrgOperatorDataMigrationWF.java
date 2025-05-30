package it.gov.pagopa.pu.migration.wf.wf.ingestion.operatordebtpostypeorg;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DebtPosTypeOrgOperatorDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
