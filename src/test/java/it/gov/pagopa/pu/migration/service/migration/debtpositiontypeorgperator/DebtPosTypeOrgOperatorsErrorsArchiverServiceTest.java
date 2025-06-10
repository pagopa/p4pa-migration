package it.gov.pagopa.pu.migration.service.migration.debtpositiontypeorgperator;

import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorErrorDTO;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.utils.faker.UploadsFaker;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class DebtPosTypeOrgOperatorsErrorsArchiverServiceTest {


  @Mock
  private FileArchiverService fileArchiverServiceMock;

  @Mock
  private CsvService csvServiceMock;

  private DebtPosTypeOrgOperatorsErrorsArchiverService service;

  public static final String FILE_NAME = "fileName";
  public static final String ERROR_CODE = "errorCode";
  public static final String ERROR_MESSAGE = "errorMessage";
  private final String errorFolder = "error";
  private final String sharedDirectory = "/tmp";
  private final MigrationFileTypeEnum fileType = MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS;

  @BeforeEach
  void setUp() {
    service = new DebtPosTypeOrgOperatorsErrorsArchiverService(sharedDirectory, errorFolder, fileArchiverServiceMock, csvServiceMock);
  }

  @Test
  void testWriteErrors_whenValidInput_thenCreatesAndArchivesCsv() throws IOException {
    List<DebtPositionTypeOrgOperatorErrorDTO> errorDTOList = List.of(
      new DebtPositionTypeOrgOperatorErrorDTO(FILE_NAME, "ipaCode1", "DPTOCode1",1L , ERROR_CODE, ERROR_MESSAGE),
      new DebtPositionTypeOrgOperatorErrorDTO(FILE_NAME, "ipaCode2","DPTOCode2", 1L, ERROR_CODE, ERROR_MESSAGE)
    );
    Path workingDirectory = Path.of("build", "test");
    Uploads upload = UploadsFaker.buildUploads(fileType);
    Path expectedErrorFilePath = workingDirectory.resolve("ERROR-fileName.csv");

    // When
    service.writeErrors(workingDirectory, upload, errorDTOList);

    // Then
    Mockito.verify(csvServiceMock)
      .createCsv(eq(expectedErrorFilePath), any(), any());
  }

  @Test
  void testWriteErrors_whenErrorListEmpty_thenReturn() throws IOException {
    Path workingDirectory = Path.of("build", "test");
    Uploads upload = UploadsFaker.buildUploads(fileType);
    Path expectedErrorFilePath = workingDirectory.resolve("ERROR-fileName.csv");

    // When
    service.writeErrors(workingDirectory, upload, List.of());

    // Then
    Mockito.verify(csvServiceMock, Mockito.times(0))
      .createCsv(eq(expectedErrorFilePath), any(), any());
  }

  @Test
  void testWriteErrors_whenIOException_thenThrowsActivitiesException() throws IOException {
    List<DebtPositionTypeOrgOperatorErrorDTO> errorDTOList = List.of(
      new DebtPositionTypeOrgOperatorErrorDTO(FILE_NAME, "ipaCode1", "DPTOCode1",1L , ERROR_CODE, ERROR_MESSAGE)
    );
    Path workingDirectory = Path.of("build", "test");
    Uploads upload = UploadsFaker.buildUploads(fileType);
    Path expectedErrorFilePath = workingDirectory.resolve("ERROR-fileName.csv");

    Mockito.doThrow(new IOException("Error creating CSV"))
      .when(csvServiceMock)
      .createCsv(eq(expectedErrorFilePath), any(), any());

    // When & Then
    NotRetryableActivityException exception = assertThrows(NotRetryableActivityException.class, () ->
      service.writeErrors(workingDirectory, upload, errorDTOList));
    assertEquals("Error creating CSV", exception.getMessage());
  }

  @Test
  void givenNoErrorsWhenArchiveErrorFilesThenReturnNull() {
    // Given
    Path workingDirectory = Path.of("build");

    // When
    String result = service.archiveErrorFiles(workingDirectory, new Uploads());

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void givenErrorsWhenArchiveErrorFilesThenCompressAndArchiveThem() throws IOException {
    // Given
    Path workingDirectory = Path.of("build", "test");
    Files.createDirectories(workingDirectory);
    Path errorFile = Files.createTempFile(workingDirectory, "ERROR-", ".csv");
    try {
      Uploads upload = UploadsFaker.buildUploads(fileType);
      String expectedZipErrorFileName = "ERROR-fileName.zip";

      // When
      String result = service.archiveErrorFiles(workingDirectory, upload);

      // Then
      Assertions.assertEquals(expectedZipErrorFileName, result);

      Mockito.verify(fileArchiverServiceMock)
        .compressAndArchive(List.of(errorFile), Path.of("build/test/" + expectedZipErrorFileName), Path.of(sharedDirectory, upload.getOrganizationId() + "", upload.getFilePathName(), errorFolder));
    } finally {
      Files.delete(errorFile);
    }
  }

  @Test
  void givenArchiveErrorFilesWhenIOExceptionThenReturnNull() throws IOException {
    // Given
    Path workingDirectory = Path.of("build", "test");
    Files.createDirectories(workingDirectory);
    Path errorFile = Files.createTempFile(workingDirectory, "ERROR-", ".csv");
    try {
      Uploads upload = UploadsFaker.buildUploads(fileType);
      String expectedZipErrorFileName = "ERROR-fileName.zip";

      Mockito.doThrow(new IOException("Error")).when(fileArchiverServiceMock)
        .compressAndArchive(List.of(errorFile), Path.of("build/test/" + expectedZipErrorFileName), Path.of(sharedDirectory, upload.getOrganizationId() + "", upload.getFilePathName(), errorFolder));

      // When
      String result = service.archiveErrorFiles(workingDirectory, upload);

      // Then
      Assertions.assertNull(result);

    } finally {
      Files.delete(errorFile);
    }
  }

}
