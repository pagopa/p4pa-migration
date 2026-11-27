package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.client.FileShareClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FileShareServiceImpl implements FileShareService {

  private final FileShareClient client;

  public FileShareServiceImpl(FileShareClient client) {
    this.client = client;
  }

  @Override
  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, Resource file, String accessToken) {
    return client.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken);
  }

  @Override
  public Resource downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, String accessToken) {
    return client.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, accessToken);
  }
}
