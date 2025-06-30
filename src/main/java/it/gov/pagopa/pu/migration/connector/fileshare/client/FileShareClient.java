package it.gov.pagopa.pu.migration.connector.fileshare.client;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.config.FileShareApisHolder;
import it.gov.pagopa.pu.migration.wf.exception.IngestionFlowFileAlreadyProcessedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class FileShareClient {

  private final FileShareApisHolder apisHolder;

  public FileShareClient(FileShareApisHolder apisHolder) {
    this.apisHolder = apisHolder;
  }

  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, Resource file, String accessToken) {
    try {
      return apisHolder.getIngestionFlowFileApi(accessToken)
        .uploadIngestionFlowFile(
          organizationId,
          ingestionFlowFileType,
          FileOrigin.SIL,
          file,
          file.getFilename(),
          null)
        .getIngestionFlowFileId();

    } catch (
      HttpClientErrorException.Conflict e) {
      String err = "Error uploading file " + file + " to FileShare: " + e.getMessage();
      log.error(err);
      throw new IngestionFlowFileAlreadyProcessedException(err);
    }
  }

  public Resource downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, String accessToken) {
    return apisHolder.getIngestionFlowFileApi(accessToken)
      .downloadIngestionFlowErrorsFile(organizationId,ingestionFlowFileId);
  }

}
