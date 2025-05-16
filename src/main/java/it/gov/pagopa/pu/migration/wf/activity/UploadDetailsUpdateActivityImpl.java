package it.gov.pagopa.pu.migration.wf.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
public class UploadDetailsUpdateActivityImpl implements UploadDetailsUpdateActivity {

  private final UploadDetailsRepository repository;

  public UploadDetailsUpdateActivityImpl(UploadDetailsRepository repository) {
    this.repository = repository;
  }

  @Override
  public UploadDetails save(UploadDetails entity) {
    log.info("Saving new upload detail on uploadId: {} related to ingestionFlowFileId:{}", entity.getUploadId(), entity.getIngestionFlowFileId());
    return repository.save(entity);
  }

  @Override
  public void updateStatus(Long uploadId, IngestionFlowFile ingestionFlowFile) {
    log.info("Updating upload detail status: uploadDetailId:{}, newStatus:{}", uploadId, ingestionFlowFile.getStatus());
    if(repository.updateStatus(uploadId, ingestionFlowFile) != 1){
      throw new UploadNotFoundException("Cannot update uploads having id " + uploadId + " to status " + ingestionFlowFile.getStatus());
    }
  }
}
