package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class UploadsStatusUpdateActivityImpl implements UploadsStatusUpdateActivity {

  private final UploadsRepository repository;

  public UploadsStatusUpdateActivityImpl(UploadsRepository repository) {
    this.repository = repository;
  }

  @Override
  public void updateStatus(Long uploadId, UploadsStatusEnum oldStatus, UploadsStatusEnum newStatus, MigrationFileResult migrationResult) {
    log.info("Updating upload status: uploadId:{}, oldStatus:{}, newStatus:{}", uploadId, oldStatus, newStatus);
    if(repository.updateStatus(uploadId, oldStatus, newStatus, migrationResult) != 1){
      throw new UploadNotFoundException("Cannot update uploads having id " + uploadId + " from status " + oldStatus + " to status " + newStatus);
    }
  }
}
