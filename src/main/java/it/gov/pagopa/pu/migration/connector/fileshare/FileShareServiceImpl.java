package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.client.FileShareClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class FileShareServiceImpl implements FileShareService {

  private final FileShareClient client;

  public FileShareServiceImpl(FileShareClient client) {
    this.client = client;
  }

  @Override
  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, Resource file, String accessToken) {
    try {
      return client.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        e.getResponseBodyAsString();
        if (e.getResponseBodyAsString().contains("\"code\":\"INVALID_FILE\"")) {
          throw new IllegalArgumentException("File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]");
        }
      }
      throw e;
    }
  }

  @Override
  public Resource downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, String accessToken) {
    return client.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, accessToken);
  }
}
