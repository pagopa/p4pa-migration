package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
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
        try {
          FileshareErrorDTO error = e.getResponseBodyAs(FileshareErrorDTO.class);
          if (error != null && "INVALID_FILE".equals(error.getCode().getValue())) {
            throw new IllegalArgumentException(error.getMessage());
          }
        } catch (IllegalStateException ex) {
          throw e;
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
