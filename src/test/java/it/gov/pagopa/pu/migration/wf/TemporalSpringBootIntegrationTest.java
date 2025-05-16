package it.gov.pagopa.pu.migration.wf;

import com.uber.m3.tally.NoopScope;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.internal.client.WorkflowClientHelper;
import it.gov.pagopa.pu.migration.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@TestPropertySource(properties = {
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password=sa",

  "spring.temporal.test-server.enabled: true",
  "spring.temporal.namespace: default",

  "workflow.migration-data-ingestion.retry-maximum-attempts: 3",
  "workflow.migration-data-ingestion.retry-maximum-interval: 100",
  "workflow.migration-data-ingestion.retry-backoff-coefficient: 1",
  "workflow.migration-data-ingestion.start-to-close-timeout-in-seconds: 100"
})
class TemporalSpringBootIntegrationTest {

  /**
   * <a href="https://docs.temporal.io/workflows#status">Closed statuses</a>
   */
  private final Set<WorkflowExecutionStatus> wfTerminationStatuses = Set.of(
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_TERMINATED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_CANCELED
  );

  @Autowired
  private WorkflowClient temporalClient;

  @Autowired
  private OrganizationDataMigrationWFClient workflowClient;

  @MockitoSpyBean
  private UploadsStatusUpdateActivity uploadsStatusUpdateActivitySpy;

  @MockitoBean
  private UploadsRepository uploadsRepositoryMock;
  @MockitoBean
  private UploadDetailsRepository uploadDetailsRepositoryMock;
  @MockitoBean
  private IngestionFlowFileService ingestionFlowFileServiceMock;

  @Test
  void givenSuccessfulUseCaseWhenExecuteWfThenInvokeAllActivities() {
    // Given
    long uploadId = 1L;
    Uploads upload = TestUtils.getPodamFactory().manufacturePojo(Uploads.class);
    upload.setUploadId(uploadId);
    upload.setFileType(null);

    MigrationFileResult expectedMigrationFileResult = MigrationFileResult.builder()
      .errorDescription("Invalid migration file type: null expected ORGANIZATIONS")
      .build();

    when(uploadsRepositoryMock.updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null))
      .thenReturn(1);
    when(uploadsRepositoryMock.updateStatus(uploadId, UploadsStatusEnum.PROCESSING, UploadsStatusEnum.ERROR, expectedMigrationFileResult))
      .thenReturn(1);
    when(uploadsRepositoryMock.findById(uploadId))
      .thenReturn(Optional.of(upload));

    // When
    WorkflowCreatedDTO wfExec = workflowClient.migrate(uploadId);

    waitUntilWfCompletion(wfExec);
  }

  @Test
  void givenNotRetryableExceptionWhenExecuteWfThenStopExecutionWithoutRetries() {
    long uploadId = 1L;
    WorkflowCreatedDTO wfExec = workflowClient.migrate(uploadId);
    waitUntilWfFailed(wfExec);

    verify(uploadsStatusUpdateActivitySpy).updateUploadStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
    verify(uploadsRepositoryMock).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
  }

  @Test
  void givenNotRetryableExceptionExtensionWhenExecuteWfThenStopExecutionWithoutRetries() {
    // Given
    long uploadId = 1L;
    when(uploadsRepositoryMock.updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null))
      .thenThrow(new NotRetryableActivityException("extension") {
      });

    // When
    WorkflowCreatedDTO wfExec = workflowClient.migrate(uploadId);
    waitUntilWfFailed(wfExec);

    // Then
    verify(uploadsStatusUpdateActivitySpy).updateUploadStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
    verify(uploadsRepositoryMock).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
  }

  @Test
  void givenRetryableExceptionWhenExecuteWfThenRetrieActivityUntilMax() {
    long uploadId = 1L;
    when(uploadsRepositoryMock.updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null))
      .thenThrow(new RuntimeException("RetryableActivityException"));

    WorkflowCreatedDTO wfExec = workflowClient.migrate(uploadId);
    waitUntilWfFailed(wfExec);

    verify(uploadsStatusUpdateActivitySpy, times(3)).updateUploadStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
    verify(uploadsRepositoryMock, times(3)).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
  }

  // PRIVATE METHODS
  private void waitUntilWfCompletion(WorkflowCreatedDTO wfExec) {
    waitUntilWfStatus(wfExec.getWorkflowId(), WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED);
  }

  private void waitUntilWfFailed(WorkflowCreatedDTO wfExec) {
    waitUntilWfStatus(wfExec.getWorkflowId(), WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED);
  }

  private void waitUntilWfStatus(String workflowId, WorkflowExecutionStatus status) {
    WorkflowExecutionInfo info;
    do {
      info = WorkflowClientHelper.describeWorkflowInstance(temporalClient.getWorkflowServiceStubs(), "default", WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), new NoopScope());
    } while (!wfTerminationStatuses.contains(info.getStatus()));

    Assertions.assertEquals(status, info.getStatus());
  }
}


