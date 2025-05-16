package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.migration.wf.exception.IngestionFlowFileNotFoundException;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class IngestionFlowFileRetrieverActivityImpl implements IngestionFlowFileRetrieverActivity {

  private final AuthnService authnService;
  private final IngestionFlowFileService ingestionFlowFileService;

  public IngestionFlowFileRetrieverActivityImpl(AuthnService authnService, IngestionFlowFileService ingestionFlowFileService) {
    this.authnService = authnService;
    this.ingestionFlowFileService = ingestionFlowFileService;
  }

  @Override
  public IngestionFlowFile getIngestionFlowFile(long ingestionFlowFileId) {
    log.info("Retrieving ingestionFlowFileId {}", ingestionFlowFileId);
    String accessToken = authnService.getAccessToken();
    IngestionFlowFile result = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);
    if(result == null){
      throw new IngestionFlowFileNotFoundException("Cannot find IngestionFlowFileResult having id " + ingestionFlowFileId);
    }
    return result;
  }
}
