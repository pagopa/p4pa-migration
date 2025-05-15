package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import org.springframework.stereotype.Service;

@ActivityImpl(taskQueues = "MIGRATION_TASK_QUEUE")
@Service
public class UploadsStatusUpdateActivityImpl implements UploadsStatusUpdateActivity {

  private final UploadsRepository repository;

  public UploadsStatusUpdateActivityImpl(UploadsRepository repository) {
    this.repository = repository;
  }

  @Override
  public void updateStatus(Long id, UploadsStatusEnum oldStatus, UploadsStatusEnum newStatus, MigrationFileResult migrationResult) {
    repository.
  }
}
