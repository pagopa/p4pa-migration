package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionErrorDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;
import it.gov.pagopa.pu.migration.wf.exception.MigrationFileProcessingException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.ErrorArchiverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionProcessingServiceTest {

  @Mock
  private DebtPositionErrorsArchiverService errorsArchiverServiceMock;
  @Mock
  private CsvService csvServiceMock;
  @Mock
  private ErrorArchiverService<DebtPositionErrorDTO> errorArchiverServiceMock;

  private DebtPositionProcessingService service;

  @BeforeEach
  void setUp() {
    service = new DebtPositionProcessingService(
      errorArchiverServiceMock,
      csvServiceMock,
      errorsArchiverServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(errorsArchiverServiceMock, csvServiceMock);
  }

  @Test
  void processDebtPositionFileReturnsResultFromCsvService() throws Exception {
    Path tempDir = Files.createTempDirectory("debtposition-test-dir-");
    Path file = Files.createTempFile(tempDir, "debtposition-test-", ".csv");
    try {
      List<InstallmentIngestionFlowFileDTO> dtos = new ArrayList<>();
      InstallmentIngestionFlowFileDTO validDto = new InstallmentIngestionFlowFileDTO();
      validDto.setIuv("IUV123");
      dtos.add(validDto);
      when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).then(invocationOnMock -> {
        BiFunction<Iterator<InstallmentIngestionFlowFileDTO>, List<CsvException>, ?> consumer = invocationOnMock.getArgument(2);
        consumer.apply(dtos.iterator(), new ArrayList<>());
        return null;
      });
      Path parsedFile = tempDir.resolve("parsed-" + file.getFileName().toString().replaceFirst("\\.[^.]+$", "") + ".csv");
      try (var filesMocked = mockStatic(Files.class)) {
        filesMocked.when(() -> Files.exists(any())).then(invocation -> {
          Path arg = invocation.getArgument(0);
          return arg.equals(parsedFile);
        });
        List<DebtPositionErrorDTO> errorList = new ArrayList<>();
        DebtPositionMigrationFileResult result = service.processMultipleDebtPositionFiles(List.of(file), mock(Uploads.class), errorList);
        assertNotNull(result);
        assertThat(result.getParsedFiles()).isNotEmpty();
        assertThat(result.getNumCorrectlyProcessedFiles()).isEqualTo(1);
        assertThat(result.getNumTotalFiles()).isEqualTo(1);
        assertThat(result.getNumTotalRows()).isEqualTo(1);
        assertThat(result.getNumCorrectlyProcessedRows()).isEqualTo(1);
        assertThat(errorList).isEmpty();
      }
      verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
    } finally {
      Files.deleteIfExists(file);
      Files.deleteIfExists(tempDir);
    }
  }

  @Test
  void processDebtPositionFileThrowsMigrationFileProcessingExceptionOnError() throws Exception {
    Path file = Path.of("file.csv");
    when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).thenThrow(new UnsupportedOperationException("fail"));
    Exception ex = assertThrows(Exception.class, () -> service.processMultipleDebtPositionFiles(List.of(file), mock(Uploads.class), new ArrayList<>()));
    assertTrue(ex instanceof UnsupportedOperationException || ex instanceof MigrationFileProcessingException);
  }

  @Test
  void readAndParseRows_handlesEmptyFileList() {
    List<DebtPositionErrorDTO> errorList = new ArrayList<>();
    DebtPositionMigrationFileResult result = service.processMultipleDebtPositionFiles(new ArrayList<>(), mock(Uploads.class), errorList);
    assertNotNull(result);
    assertThat(result.getParsedFiles()).isEmpty();
    assertThat(result.getNumCorrectlyProcessedFiles()).isZero();
    assertThat(result.getNumTotalFiles()).isZero();
    assertThat(result.getErrorDescription()).isNull();
    verifyNoInteractions(csvServiceMock);
  }

  @Test
  void readAndParseRows_handlesNullErrorList() throws IOException {
    Path file = Files.createTempFile("debtposition-test-", ".csv");
    try {
      List<InstallmentIngestionFlowFileDTO> dtos = new ArrayList<>();
      InstallmentIngestionFlowFileDTO validDto = new InstallmentIngestionFlowFileDTO();
      validDto.setIuv("IUV123");
      dtos.add(validDto);
      when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).then(invocationOnMock -> {
        BiFunction<Iterator<InstallmentIngestionFlowFileDTO>, List<CsvException>, ?> consumer = invocationOnMock.getArgument(2);
        consumer.apply(dtos.iterator(), new ArrayList<>());
        return null;
      });
      try (var filesMocked = mockStatic(Files.class)) {
        filesMocked.when(() -> Files.exists(any())).thenReturn(true);
        DebtPositionMigrationFileResult result = service.processMultipleDebtPositionFiles(List.of(file), mock(Uploads.class), null);
        assertNotNull(result);
        assertThat(result.getParsedFiles()).isNotEmpty();
        assertThat(result.getNumCorrectlyProcessedFiles()).isEqualTo(1);
        assertThat(result.getNumTotalFiles()).isEqualTo(1);
        assertThat(result.getNumTotalRows()).isEqualTo(1);
        assertThat(result.getNumCorrectlyProcessedRows()).isEqualTo(1);
      }
      verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
    } finally {
      Files.deleteIfExists(file);
    }
  }

  @Test
  void readAndParseRows_populatesErrorListOnCsvException() throws IOException {
    Path file = Files.createTempFile("debtposition-test-", ".csv");
    try {
      List<DebtPositionErrorDTO> errorList = new ArrayList<>();
      List<CsvException> csvExceptions = List.of(new CsvException("csv error 1"), new CsvException("csv error 2"));

      Iterator<?> mockIterator = mock(Iterator.class);
      when(mockIterator.hasNext()).thenReturn(false);

      when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).then(invocation -> {
        BiFunction callback = invocation.getArgument(2);
        callback.apply(mockIterator, csvExceptions);
        return null;
      });

      DebtPositionMigrationFileResult result = service.processMultipleDebtPositionFiles(List.of(file), mock(Uploads.class), errorList);

      assertEquals(1, result.getNumTotalFiles());
      assertThat(result.getParsedFiles()).isEmpty();
      assertEquals(0, result.getNumCorrectlyProcessedFiles());
      assertEquals(2, errorList.size());
      assertEquals(file.getFileName().toString(), errorList.get(0).getFileName());
      assertTrue(errorList.get(0).getErrorMessage().contains("csv error 1"));
      assertEquals(file.getFileName().toString(), errorList.get(1).getFileName());
      assertTrue(errorList.get(1).getErrorMessage().contains("csv error 2"));
      assertTrue(result.getErrorDescription().contains("csv error 1"));
      verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
    } finally {
      Files.deleteIfExists(file);
      Path parsed = file.getParent().resolve(file.getFileName().toString().replaceFirst("\\.[^.]+$", "") + "-parsed.csv");
      Files.deleteIfExists(parsed);
    }
  }


}
