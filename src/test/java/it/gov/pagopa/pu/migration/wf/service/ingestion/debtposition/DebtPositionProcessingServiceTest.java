package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionProcessingServiceTest {

  @Mock
  private DebtPositionErrorsArchiverService errorsArchiverServiceMock;
  @Mock
  private Path workingDirectory;
  @Mock
  private OrganizationService organizationServiceMock;
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
    Path file = Path.of("file.csv");
    when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).thenReturn(null);
    DebtPositionMigrationFileResult result = service.readAndParseRows(List.of(file), List.of());
    assertNotNull(result);
    assertThat(result.getParsedFiles()).isNotEmpty();
    assertThat(result.getNumCorrectlyProcessedFiles()).isEqualTo(1);
    assertThat(result.getNumTotalFiles()).isEqualTo(1);
    verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
    verify(csvServiceMock).createCsv(any(), eq(InstallmentIngestionFlowFileDTO.class), any(), eq("V2_0"));
  }

  @Test
  void processDebtPositionFileThrowsMigrationFileProcessingExceptionOnError() throws Exception {
    Path file = Path.of("file.csv");
    when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).thenThrow(new UnsupportedOperationException("fail"));
    Exception ex = assertThrows(Exception.class, () -> service.readAndParseRows(List.of(file), List.of()));
    assertTrue(ex instanceof UnsupportedOperationException || ex instanceof MigrationFileProcessingException);
  }

  @Test
  void readAndParseRows_handlesEmptyFileList() {
    List<DebtPositionErrorDTO> errorList = new ArrayList<>();
    DebtPositionMigrationFileResult result = service.readAndParseRows(List.of(), errorList);
    assertNotNull(result);
    assertThat(result.getParsedFiles()).isEmpty();
    assertThat(result.getNumCorrectlyProcessedFiles()).isZero();
    assertThat(result.getNumTotalFiles()).isZero();
    assertThat(result.getErrorDescription()).isNull();
    verifyNoInteractions(csvServiceMock);
  }

  @Test
  void readAndParseRows_handlesNullErrorList() throws IOException {
    Path file = Path.of("file.csv");
    when(csvServiceMock.readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any())).thenReturn(null);
    DebtPositionMigrationFileResult result = service.readAndParseRows(List.of(file), null);
    assertNotNull(result);
    assertThat(result.getParsedFiles()).isNotEmpty();
    assertThat(result.getNumCorrectlyProcessedFiles()).isEqualTo(1);
    assertThat(result.getNumTotalFiles()).isEqualTo(1);
    verify(csvServiceMock).readCsv(eq(file), eq(InstallmentIngestionFlowFileDTO.class), any());
    verify(csvServiceMock).createCsv(any(), eq(InstallmentIngestionFlowFileDTO.class), any(), eq("V2_0"));
  }


}
