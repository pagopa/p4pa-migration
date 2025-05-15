package it.gov.pagopa.pu.migration.connector.processexecutions;

import it.gov.pagopa.pu.migration.connector.processexecutions.client.IngestionFlowFileEntityClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import org.springframework.stereotype.Service;

@Service
public class IngestionFlowFileServiceImpl implements IngestionFlowFileService {

  private final IngestionFlowFileEntityClient entityClient;

  public IngestionFlowFileServiceImpl(IngestionFlowFileEntityClient entityClient) {
    this.entityClient = entityClient;
  }

  @Override
  public IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken) {
    return entityClient.getIngestionFlowFile(ingestionFlowFileId, accessToken);
  }

}
