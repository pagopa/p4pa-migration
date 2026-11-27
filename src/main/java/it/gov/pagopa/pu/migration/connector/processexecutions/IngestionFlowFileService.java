package it.gov.pagopa.pu.migration.connector.processexecutions;

import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;

public interface IngestionFlowFileService {
  IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken);
}
