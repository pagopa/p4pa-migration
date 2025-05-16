package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;

/** To save or update {@link it.gov.pagopa.pu.migration.model.UploadDetails} */
@ActivityInterface
public interface UploadDetailsUpdateActivity {

  @ActivityMethod
  UploadDetails save(UploadDetails entity);
  @ActivityMethod
  void updateStatus(Long uploadDetailId, IngestionFlowFile ingestionFlowFile);
}
