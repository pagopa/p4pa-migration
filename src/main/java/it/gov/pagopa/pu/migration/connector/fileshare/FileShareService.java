package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import org.springframework.core.io.Resource;

public interface FileShareService {
  Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, Resource file, String accessToken);
}
