package it.gov.pagopa.pu.migration.connector.processexecutions.client;

import it.gov.pagopa.pu.migration.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class IngestionFlowFileEntityClient {

  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileEntityClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken) {
    try {
      log.debug("Fetching ingestion flow file with ID [{}]", ingestionFlowFileId);
      return processExecutionsApisHolder.getIngestionFlowFileEntityControllerApi(accessToken)
        .crudGetIngestionflowfile(String.valueOf(ingestionFlowFileId));
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Cannot find IngestionFlowFile with ID [{}]", ingestionFlowFileId);
      return null;
    }
  }

  public IngestionFlowFile findById(Long ingestionFlowFileId, String accessToken) {
    try{
      return processExecutionsApisHolder.getIngestionFlowFileEntityControllerApi(accessToken)
        .crudGetIngestionflowfile(String.valueOf(ingestionFlowFileId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find IngestionFlowFile having id {}", ingestionFlowFileId);
      return null;
    }
  }

}
