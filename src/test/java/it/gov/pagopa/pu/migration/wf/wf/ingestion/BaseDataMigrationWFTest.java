package it.gov.pagopa.pu.migration.wf.wf.ingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.mapper.UploadDetailsMapper;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.migration.wf.activity.IngestionFlowFileRetrieverActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadDetailsUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public abstract class BaseDataMigrationWFTest<A extends MigrationFileTypeHandlerActivity> {

  @Mock
  private UploadsStatusUpdateActivity uploadsStatusUpdateActivityMock;
  @Mock
  private UploadDetailsUpdateActivity uploadDetailsUpdateActivityMock;
  @Mock
  private IngestionFlowFileRetrieverActivity ingestionFlowFileRetrieverActivityMock;
  private A migrationFileTypeHandlerActivityMock;

  private BaseDataMigrationWFImpl wf;

  @BeforeEach
  void init() {
    DataMigrationWfConfig configMock = mock(DataMigrationWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    Mockito.when(configMock.buildUploadsStatusUpdateActivityStub()).thenReturn(uploadsStatusUpdateActivityMock);
    Mockito.when(configMock.buildIngestionFlowFileRetrieverActivityStub()).thenReturn(ingestionFlowFileRetrieverActivityMock);
    Mockito.when(configMock.buildUploadDetailsUpdateActivityStub()).thenReturn(uploadDetailsUpdateActivityMock);

    Pair<OngoingStubbing<A>, Class<A>> stub2mockClass = getMigrationFileTypeHandlerActivityMockConfiguration(configMock);
    migrationFileTypeHandlerActivityMock = Mockito.mock(stub2mockClass.getRight());
    stub2mockClass.getLeft()
      .thenReturn(migrationFileTypeHandlerActivityMock);

    Mockito.when(applicationContextMock.getBean(DataMigrationWfConfig.class)).thenReturn(configMock);

    wf = buildWf();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      uploadsStatusUpdateActivityMock,
      uploadDetailsUpdateActivityMock,
      ingestionFlowFileRetrieverActivityMock,
      migrationFileTypeHandlerActivityMock
    );
  }

  protected abstract BaseDataMigrationWFImpl buildWf();

  protected abstract Pair<OngoingStubbing<A>, Class<A>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock);

  @Test
  void givenExceptionDuringFileHandlingWhenMigrateThenSetErrorStatus() {
    // Given
    long uploadId = 0L;
    MigrationFileResult expectedResult = MigrationFileResult.builder()
      .errorDescription("DUMMY")
      .build();

    Mockito.when(migrationFileTypeHandlerActivityMock.processFile(uploadId))
      .thenThrow(new RuntimeException("DUMMY"));

    // When
    wf.migrate(uploadId);

    // Then
    Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
    Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.PROCESSING, UploadsStatusEnum.ERROR, expectedResult);
  }

  @Test
  void givenErrorDuringIngestionFlowFileProcessingWhenMigrateThenSetErrorStatus() {
    // Given
    long uploadId = 0L;
    long ingestionFlowFileId = 10L;
    long uploadDetailId = 100L;

    IngestionFlowFile ingestionFlowFileAtStart = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
      .ingestionFlowFileId(ingestionFlowFileId);
    MigrationFileResult expectedResult = MigrationFileResult.builder()
      .numTotalFiles(2)
      .numCorrectlyProcessedFiles(1)
      .fileSize(3L)
      .ingestionFlowFiles(List.of(
        ingestionFlowFileAtStart
      ))
      .build();

    UploadDetails storedUploadDetails = new UploadDetails();
    storedUploadDetails.setUploadDetailId(uploadDetailId);
    storedUploadDetails.setIngestionFlowFileId(ingestionFlowFileId);

    IngestionFlowFile ingestionFlowFileError = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
      .ingestionFlowFileId(ingestionFlowFileId)
      .ingestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS)
      .errorDescription("DUMMY")
      .status(IngestionFlowFileStatus.ERROR);

    Mockito.when(migrationFileTypeHandlerActivityMock.processFile(uploadId))
      .thenReturn(expectedResult);

    Mockito.when(uploadDetailsUpdateActivityMock.save(UploadDetailsMapper.map(uploadId, ingestionFlowFileAtStart)))
      .thenReturn(storedUploadDetails);

    Mockito.when(ingestionFlowFileRetrieverActivityMock.getIngestionFlowFile(ingestionFlowFileId))
      .thenReturn(ingestionFlowFileError);

    try (MockedStatic<Workflow> workflowMockedStatic = Mockito.mockStatic(Workflow.class)) {
      // When
      wf.migrate(uploadId);

      // Then
      Assertions.assertEquals(
        """
        Something went wrong while waiting upload details processing:
        An error occurred while importing ingestionFlowFileId 10 having type DP_INSTALLMENTS: DUMMY""",
        expectedResult.getErrorDescription());
      Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
      Mockito.verify(uploadDetailsUpdateActivityMock).updateStatus(uploadDetailId, ingestionFlowFileError);
      Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.PROCESSING, UploadsStatusEnum.ERROR, expectedResult);

      workflowMockedStatic.verifyNoMoreInteractions();
    }
  }

  @Test
  void whenMigrateThenOk() {
    // Given
    long uploadId = 0L;
    long ingestionFlowFileId = 10L;
    long uploadDetailId = 100L;

    IngestionFlowFile ingestionFlowFileAtStart = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
      .ingestionFlowFileId(ingestionFlowFileId);
    MigrationFileResult expectedResult = MigrationFileResult.builder()
      .numTotalFiles(2)
      .numCorrectlyProcessedFiles(1)
      .fileSize(3L)
      .ingestionFlowFiles(List.of(
        ingestionFlowFileAtStart
      ))
      .build();

    UploadDetails storedUploadDetails = new UploadDetails();
    storedUploadDetails.setUploadDetailId(uploadDetailId);
    storedUploadDetails.setIngestionFlowFileId(ingestionFlowFileId);

    IngestionFlowFile ingestionFlowFileProcessing = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
      .ingestionFlowFileId(ingestionFlowFileId)
      .errorDescription(null)
      .status(IngestionFlowFileStatus.PROCESSING);
    IngestionFlowFile ingestionFlowFileCompleted = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
      .ingestionFlowFileId(ingestionFlowFileId)
      .errorDescription(null)
      .status(IngestionFlowFileStatus.COMPLETED);

    Mockito.when(migrationFileTypeHandlerActivityMock.processFile(uploadId))
      .thenReturn(expectedResult);

    Mockito.when(uploadDetailsUpdateActivityMock.save(UploadDetailsMapper.map(uploadId, ingestionFlowFileAtStart)))
        .thenReturn(storedUploadDetails);

    Mockito.when(ingestionFlowFileRetrieverActivityMock.getIngestionFlowFile(ingestionFlowFileId))
      .thenReturn(ingestionFlowFileProcessing)
      .thenReturn(ingestionFlowFileProcessing)
      .thenReturn(ingestionFlowFileCompleted);

    try (MockedStatic<Workflow> workflowMockedStatic = Mockito.mockStatic(Workflow.class)) {
      // When
      wf.migrate(uploadId);

      // Then
      Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.UPLOADED, UploadsStatusEnum.PROCESSING, null);
      Mockito.verify(uploadDetailsUpdateActivityMock).updateStatus(uploadDetailId, ingestionFlowFileCompleted);
      Mockito.verify(uploadsStatusUpdateActivityMock).updateStatus(uploadId, UploadsStatusEnum.PROCESSING, UploadsStatusEnum.COMPLETED, expectedResult);
      workflowMockedStatic.verify(() -> Workflow.sleep(Duration.ofMinutes(5)), times(2));
    }
  }
}
