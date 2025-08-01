package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositions;

import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition.DebtPositionProcessingService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionsMigrationFileTypeHandlerActivityTest {

  @Mock
  private UploadsRepository uploadsRepositoryMock;
  @Mock
  private MigrationFileRetrieverService fileRetrieverServiceMock;
  @Mock
  private FileArchiverService fileArchiverServiceMock;
  @Mock
  private FileShareService fileShareServiceMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private ZipFileService zipFileServiceMock;
  @Mock
  private DebtPositionProcessingService debtPositionProcessingServiceMock;


  private DebtPositionsMigrationFileTypeHandlerActivityImpl activity;

  @BeforeEach
  void setUp() {
    activity = new DebtPositionsMigrationFileTypeHandlerActivityImpl(
      uploadsRepositoryMock,
      fileRetrieverServiceMock,
      fileArchiverServiceMock,
      fileShareServiceMock,
      authnServiceMock,
      organizationServiceMock,
      zipFileServiceMock,
      debtPositionProcessingServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      uploadsRepositoryMock,
      fileRetrieverServiceMock,
      fileArchiverServiceMock,
      fileShareServiceMock,
      authnServiceMock,
      organizationServiceMock,
      zipFileServiceMock,
      debtPositionProcessingServiceMock
    );
  }


  @Test
  void testHandleRetrievedFiles_success(@TempDir Path sourceDir) throws Exception {
    Path file1 = Files.createFile(sourceDir.resolve("IPA12345-file1.txt"));

    Path zipFilePath = Files.createFile(sourceDir.resolve("output.zip"));
    File mockZippedFile = zipFilePath.toFile();
    Path mockEncryptedFile = Files.copy(zipFilePath, sourceDir.resolve(zipFilePath.getFileName() + AESUtils.CIPHER_EXTENSION));

    when(uploadsRepositoryMock.findById(1L)).thenReturn(Optional.of(
      Uploads.builder()
        .organizationId(1L)
        .fileType(MigrationFileTypeEnum.DEBT_POSITIONS)
        .fileName("IPA12345-file1.txt")
        .filePathName(sourceDir.toString())
        .fileSize(123L)
        .updateOperatorExternalId("user")
        .build()
    ));
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    Organization org = new Organization();
    org.setIpaCode("IPA12345");
    org.setBrokerId(1L);
    org.setOrganizationId(10L);
    when(organizationServiceMock.getOrganizationByIpaCode(eq("IPA12345"), anyString())).thenReturn(Optional.of(org));

    when(authnServiceMock.getAccessToken("IPA12345")).thenReturn("tokenOrg");
    when(fileShareServiceMock.uploadIngestionFlowFile(
      anyLong(),
      any(),
      any(FileSystemResource.class),
      anyString()
    )).thenReturn(1L);

    String fileName = file1.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    Path zipFilePathForMock = file1.getParent().resolve(baseName + ".zip");

    when(zipFileServiceMock.zipper(
      zipFilePathForMock,
      List.of(file1)
    )).thenReturn(mockZippedFile);

    assertTrue(Files.exists(mockEncryptedFile));
    when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any()))
      .thenReturn(List.of(file1));


    List<Path> parsedFiles = List.of(file1);
    DebtPositionMigrationFileResult migrationFileResult = DebtPositionMigrationFileResult.builder()
      .parsedFiles(parsedFiles)
      .numCorrectlyProcessedFiles(1)
      .numTotalFiles(1)
      .errorDescription(null)
      .build();
    when(debtPositionProcessingServiceMock.readAndParseRows(anyList(), anyList())).thenReturn(migrationFileResult);

    MigrationFileResult result = activity.processFile(1L);
    assertNotNull(result);
    assertNotNull(result.getIngestionFlowFiles());
    assertNotNull(result.getIngestionFlowFiles().getFirst().getOrganizationId());
    verify(fileArchiverServiceMock).archive(any(Uploads.class));
  }
}
