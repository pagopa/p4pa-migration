package it.gov.pagopa.pu.migration.wf.wf.ingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.mapper.UploadDetailsMapper;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.wf.activity.IngestionFlowFileRetrieverActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadDetailsUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.utils.WfUtilities;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class BaseDataMigrationWFImpl implements ApplicationContextAware {

  private static final Set<IngestionFlowFileStatus> INGESTION_FLOW_FILE_TERMINAL_STATUSES = Set.of(
    IngestionFlowFileStatus.COMPLETED,
    IngestionFlowFileStatus.ERROR
  );
  private static final Duration SLEEP_BETWEEN_INGESTION_FLOW_STATUS_CHECK = Duration.ofMinutes(5);

  private UploadsStatusUpdateActivity uploadsStatusUpdateActivity;
  private UploadDetailsUpdateActivity uploadDetailsUpdateActivity;
  private IngestionFlowFileRetrieverActivity ingestionFlowFileRetrieverActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DataMigrationWfConfig wfConfig = applicationContext.getBean(DataMigrationWfConfig.class);

    uploadsStatusUpdateActivity = wfConfig.buildUploadsStatusUpdateActivityStub();
    uploadDetailsUpdateActivity = wfConfig.buildUploadDetailsUpdateActivityStub();
    ingestionFlowFileRetrieverActivity = wfConfig.buildIngestionFlowFileRetrieverActivityStub();

    buildActivities(wfConfig);
  }

  /**
   * to be overridden by extended class in order to build further required activities
   */
  protected void buildActivities(DataMigrationWfConfig wfConfig) {
  }

  protected abstract MigrationFileTypeHandlerActivity getMigrationFileTypeHandlerActivity();

  public void migrate(long uploadId) {
    log.info("Starting OrganizationDataMigrationWF on uploadId {}", uploadId);
    // FIXME: could start a new ingestion if there other organizationId uploads PROCESSING?
    //  Or should we await as done in it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWFImpl
    uploadsStatusUpdateActivity.updateUploadStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);

    MigrationFileResult result;
    try {
      log.info("Processing files related to uploadId {}", uploadId);
      result = getMigrationFileTypeHandlerActivity().processFile(uploadId);
    } catch (Exception e){
      log.error("Something gone wrong while processing uploadId {}", uploadId, e);
      result = MigrationFileResult.builder()
        .errorDescription(WfUtilities.getWorkflowExceptionMessage(e))
        .build();
    }

    if(result.getErrorDescription() == null) {
      log.info("Files related to uploadId {} handled successfully", uploadId);
      List<UploadDetails> details = result.getIngestionFlowFiles().stream()
        .map(i -> uploadDetailsUpdateActivity.saveDetail(UploadDetailsMapper.map(uploadId, i)))
        .toList();

      List<String> errors = waitProcessingAndUpdateDetails(uploadId, details);
      if(!CollectionUtils.isEmpty(errors)){
        result.setErrorDescription("Something went wrong while waiting upload details processing:\n" + String.join("\n", errors));
      }
    }

    UploadsStatusEnum newStatus = result.getErrorDescription()!=null
      ? UploadsStatusEnum.ERROR
      : UploadsStatusEnum.COMPLETED;
    uploadsStatusUpdateActivity.updateUploadStatus(uploadId, UploadsStatusEnum.PROCESSING, newStatus, result);
  }

  private List<String> waitProcessingAndUpdateDetails(long uploadId, List<UploadDetails> details) {
    List<String> errors = new ArrayList<>();
    int[] attemptCounter = {0};
    for (UploadDetails detail : details) {
      IngestionFlowFile ingestionFlowFile = waitIngestionFlowFileProcessing(uploadId, detail, attemptCounter);
      log.info("IngestionFlowFileId {} having type {} terminated with status {}",
        ingestionFlowFile.getIngestionFlowFileId(),
        ingestionFlowFile.getIngestionFlowFileType(),
        ingestionFlowFile.getStatus());
      uploadDetailsUpdateActivity.updateDetailStatus(detail.getUploadDetailId(), ingestionFlowFile);
      if(ingestionFlowFile.getErrorDescription()!=null){
        errors.add("An error occurred while importing ingestionFlowFileId %d having type %s: %s".formatted(
          ingestionFlowFile.getIngestionFlowFileId(),
          ingestionFlowFile.getIngestionFlowFileType(),
          ingestionFlowFile.getErrorDescription()));
      }
    }
    return errors;
  }

  private IngestionFlowFile waitIngestionFlowFileProcessing(long uploadId, UploadDetails detail, int[] attemptCounter) {
    IngestionFlowFile ingestionFlowFile;
    while ((ingestionFlowFile = ingestionFlowFileRetrieverActivity.getIngestionFlowFile(detail.getIngestionFlowFileId())) != null &&
      !INGESTION_FLOW_FILE_TERMINAL_STATUSES.contains(ingestionFlowFile.getStatus())) {
      attemptCounter[0]++;

      if (attemptCounter[0] >= WfConstants.THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW) {
        log.info("Max attempts reached, continuing as new for uploadId {}", uploadId);
        Workflow.continueAsNew(uploadId);
      }

      log.info("IngestionFlowFile status not terminated ({}), retrying for ingestionFlowFileId {}",
        ingestionFlowFile.getStatus(), ingestionFlowFile.getIngestionFlowFileId());
      Workflow.sleep(SLEEP_BETWEEN_INGESTION_FLOW_STATUS_CHECK);
    }

    return ingestionFlowFile;
  }
}
