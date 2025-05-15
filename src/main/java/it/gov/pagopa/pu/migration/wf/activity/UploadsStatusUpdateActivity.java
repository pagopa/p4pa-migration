package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

@ActivityInterface
public interface UploadsStatusUpdateActivity {

  @ActivityMethod
  void updateStatus(Long id, UploadsStatusEnum oldStatus, UploadsStatusEnum newStatus, MigrationFileResult migrationResult);
}
