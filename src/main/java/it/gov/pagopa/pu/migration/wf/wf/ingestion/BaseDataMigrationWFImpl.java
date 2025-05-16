package it.gov.pagopa.pu.migration.wf.wf.ingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.wf.activity.IngestionFlowFileRetrieverActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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

  private UploadsStatusUpdateActivity updateIngestionFlowStatusActivity;
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

    updateIngestionFlowStatusActivity = wfConfig.buildUploadsStatusUpdateActivityStub();
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
    updateIngestionFlowStatusActivity.updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);

    MigrationFileResult result;
    try {
      result = getMigrationFileTypeHandlerActivity().processFile(uploadId);
    } catch (Exception e){
      log.error("Something gone wrong while processing uploadId {}", uploadId, e);
      result = MigrationFileResult.builder()
        .errorDescription(e.getMessage())
        .build();
    }

    if(result.getErrorDescription() == null) {
        // TODO: store UploadDetail for each result.ingestionFlowFileIds

      List<String> errors = waitProcessingAndUpdateDetails(uploadId, result);
      if(!CollectionUtils.isEmpty(errors)){
        result.setErrorDescription("Something went wrong while waiting upload details processing:\n" + String.join("\n", errors));
      }
    }

    UploadsStatusEnum newStatus = result.getErrorDescription()!=null
      ? UploadsStatusEnum.ERROR
      : UploadsStatusEnum.COMPLETED;
    updateIngestionFlowStatusActivity.updateStatus(uploadId, UploadsStatusEnum.PROCESSING, newStatus, result);
  }

  private List<String> waitProcessingAndUpdateDetails(long uploadId, MigrationFileResult result) {
    List<String> errors = new ArrayList<>();
    int[] attemptCounter = {0};
    for (Pair<Long, IngestionFlowFileType> ingestionFlowId : result.getIngestionFlowFileIds()) { // FIXME cycle on UploadDetail instead
      waitIngestionFlowFileProcessing(uploadId, ingestionFlowId, attemptCounter);

      // TODO update UploadDetail
    }
    return errors;
  }

  private void waitIngestionFlowFileProcessing(long uploadId, Pair<Long, IngestionFlowFileType> ingestionFlowId, int[] attemptCounter) {
    IngestionFlowFile ingestionFlowFile;
    while ((ingestionFlowFile = ingestionFlowFileRetrieverActivity.getIngestionFlowFile(ingestionFlowId.getKey())) != null &&
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
  }
}
