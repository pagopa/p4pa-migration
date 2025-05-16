package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;

@ActivityInterface
public interface IngestionFlowFileRetrieverActivity {
  @ActivityMethod
  IngestionFlowFile getIngestionFlowFile(long ingestionFlowFileId);
}
