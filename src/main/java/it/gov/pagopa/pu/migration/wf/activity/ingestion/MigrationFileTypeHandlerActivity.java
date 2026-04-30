package it.gov.pagopa.pu.migration.wf.activity.ingestion;

import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

public interface MigrationFileTypeHandlerActivity {
  MigrationFileResult processFile(Long uploadId);
}
