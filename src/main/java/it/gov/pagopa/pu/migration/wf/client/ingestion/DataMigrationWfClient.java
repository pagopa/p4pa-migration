package it.gov.pagopa.pu.migration.wf.client.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;

public interface DataMigrationWfClient {
  WorkflowCreatedDTO migrate(Long uploadId);
}
