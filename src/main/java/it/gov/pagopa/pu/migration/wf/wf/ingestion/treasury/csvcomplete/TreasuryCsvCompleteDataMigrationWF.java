package it.gov.pagopa.pu.migration.wf.wf.ingestion.treasury.csvcomplete;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TreasuryCsvCompleteDataMigrationWF {
  @WorkflowMethod
  void migrate(long uploadId);
}
