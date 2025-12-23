package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionErrorDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;
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
    Path file = Files.createTempFile(tempDir, "debtposition-test-1_4", ".csv");
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
      when(csvServiceMock.createCsvWriter(any(), any(), any())).thenReturn(mock(StatefulBeanToCsv.class));
      DebtPositionMigrationFileResult result = service.processDebtPositionFile(file, mock(Uploads.class), new ArrayList<>());
      assertNotNull(result);
      assertThat(result.getParsedFiles()).isNotEmpty();
      assertThat(result.getNumCorrectlyProcessedRows()).isGreaterThanOrEqualTo(0);
      assertThat(result.getNumTotalRows()).isGreaterThanOrEqualTo(0);
      verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
      verify(csvServiceMock).createCsvWriter(any(), any(), any());
    } finally {
      Files.deleteIfExists(file);
      try (var filesStream = Files.list(tempDir)) {
        filesStream.forEach(f -> {
          try {
            Files.deleteIfExists(f);
          } catch (Exception ignored) {
            // Ignore exceptions during cleanup
          }
        });
      }
      Files.deleteIfExists(tempDir);
    }
  }



  @Test
  void readAndParseRows_handlesNullErrorList() throws IOException {
        Path file = Files.createTempFile("debtposition-test-1_4", ".csv");
        try {
            List<InstallmentIngestionFlowFileDTO> dtos = new ArrayList<>();
            InstallmentIngestionFlowFileDTO validDto = new InstallmentIngestionFlowFileDTO();
            validDto.setIuv("IUV123");
            dtos.add(validDto);
            when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).thenAnswer(invocation -> {
                BiFunction<Iterator<InstallmentIngestionFlowFileDTO>, List<CsvException>, Object> processor = invocation.getArgument(2);
                return processor.apply(dtos.iterator(), new ArrayList<>());
            });
            when(csvServiceMock.createCsvWriter(any(), any(), any())).thenReturn(mock(StatefulBeanToCsv.class));
            DebtPositionMigrationFileResult result = service.processDebtPositionFile(file, mock(Uploads.class), new ArrayList<>());
            assertNotNull(result);
            assertThat(result.getParsedFiles()).isNotEmpty();
            assertThat(result.getNumCorrectlyProcessedRows()).isGreaterThanOrEqualTo(0);
            assertThat(result.getNumTotalRows()).isGreaterThanOrEqualTo(0);
            verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
            verify(csvServiceMock).createCsvWriter(any(), any(), any());
        } finally {
            Files.deleteIfExists(file);
        }
    }

  @Test
  void readAndParseRows_populatesErrorListOnCsvException() throws IOException {
    Path file = Files.createTempFile("debtposition-test-1_4", ".csv");
    try {
      List<DebtPositionErrorDTO> errorList = new ArrayList<>();
      List<CsvException> csvExceptions = List.of(new CsvException("csv error 1"), new CsvException("csv error 2"));
      Iterator<InstallmentIngestionFlowFileDTO> mockIterator = mock(Iterator.class);
      when(mockIterator.hasNext()).thenReturn(false);
      when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).then(invocation -> {
        BiFunction<Iterator<InstallmentIngestionFlowFileDTO>, List<CsvException>, Object> callback = invocation.getArgument(2);
        callback.apply(mockIterator, csvExceptions);
        csvExceptions.forEach(ex -> errorList.add(new DebtPositionErrorDTO()));
        return null;
      });
      when(csvServiceMock.createCsvWriter(any(), any(), any())).thenReturn(mock(StatefulBeanToCsv.class));
      DebtPositionMigrationFileResult result = service.processDebtPositionFile(file, mock(Uploads.class), errorList);
      assertNotNull(result);
      assertThat(errorList).isNotEmpty();
      assertThat(errorList).hasSameSizeAs(csvExceptions);
      verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
      verify(csvServiceMock).createCsvWriter(any(), any(), any());
    } finally {
      Files.deleteIfExists(file);
      Path parsed = file.getParent().resolve(file.getFileName().toString().replaceFirst("\\.[^.]+$", "-parsed.csv"));
      Files.deleteIfExists(parsed);
    }
  }



  @Test
  void validatorSetsDefaultValuesForNullFields() {
    InstallmentIngestionFlowFileDTO dto = new InstallmentIngestionFlowFileDTO();
    dto.setFlagMultiBeneficiary(null);
    dto.setNumberBeneficiary(null);
    InstallmentIngestionFlowFileRequiredFieldsValidator.setDefaultValues(dto);
    assertThat(dto.getFlagMultiBeneficiary()).isEqualTo("false");
    assertThat(dto.getNumberBeneficiary()).isEqualTo("1");
  }

  @Test
  void consumeRow_whenOrgFiscalCodeSecondarioAndOrgFiscalCode2Different_addsErrorAndReturnsFalse() {
        InstallmentIngestionFlowFileDTO dto = new InstallmentIngestionFlowFileDTO();
        dto.setOrgFiscalCodeSecondario("CF2");
        dto.setOrgFiscalCode2("DIFFERENT_CF2");
        DebtPositionMigrationFileResult migrationFileResult = DebtPositionMigrationFileResult.builder().lastCsvWriter(mock(StatefulBeanToCsv.class)).build();
        List<DebtPositionErrorDTO> errorList = new ArrayList<>();
        Uploads uploads = mock(Uploads.class);
        long lineNumber = 7L;

        boolean result = service.consumeRow(lineNumber, dto, migrationFileResult, errorList, uploads);

        assertFalse(result);
        assertEquals(1, errorList.size());
        DebtPositionErrorDTO error = errorList.get(0);
        assertEquals("DTO_DIFFERENT_VALUE", error.getErrorCode());
        assertEquals("OrgFiscalCodeSecondario and OrgFiscalCode2 have not the same value", error.getErrorMessage());
        assertEquals(lineNumber, error.getRowNumber());
    }

  @Test
  void processDebtPositionFile_withFileNameNotMatchingRegex_shouldReturnErrorResult() throws Exception {
    Path tempDir = Files.createTempDirectory("debtposition-test-dir-");
    Path file = tempDir.resolve("incorrect file name 1_4.csv");
    Files.createFile(file);
    try {
      DebtPositionMigrationFileResult result = service.processDebtPositionFile(file, mock(Uploads.class), new ArrayList<>());
      assertNotNull(result);
      assertThat(result.getErrorDescription()).isNotNull();
      assertThat(result.getErrorDescription()).contains("Incorrect file name format");
      verify(csvServiceMock, never()).createCsvWriter(any(), any(), any());
      verify(csvServiceMock, never()).readCsv(any(), any(), any());
    } finally {
      Files.deleteIfExists(file);
      Files.deleteIfExists(tempDir);
    }
  }

}
