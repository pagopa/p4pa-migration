package it.gov.pagopa.pu.migration.connector.fileshare.client;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.config.FileShareApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileShareClient {

  private final FileShareApisHolder apisHolder;

  public FileShareClient(FileShareApisHolder apisHolder){
    this.apisHolder = apisHolder;
  }

  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, Resource file, String accessToken) {
    return apisHolder.getIngestionFlowFileApi(accessToken)
      .uploadIngestionFlowFile(
        organizationId,
        ingestionFlowFileType,
        FileOrigin.SIL,
        file,
        file.getFilename(),
        null)
      .getIngestionFlowFileId();
  }

  public Resource downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, String accessToken) {
    return apisHolder.getIngestionFlowFileApi(accessToken)
      .downloadIngestionFlowErrorsFile(organizationId,ingestionFlowFileId);
  }

}
