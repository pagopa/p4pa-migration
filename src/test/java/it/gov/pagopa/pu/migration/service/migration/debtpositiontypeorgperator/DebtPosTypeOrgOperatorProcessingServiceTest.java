package it.gov.pagopa.pu.migration.service.migration.debtpositiontypeorgperator;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.debtposition.DebtPositionTypeOrgService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorErrorDTO;
import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.exception.MigrationFileProcessingException;
import it.gov.pagopa.pu.migration.mapper.DebtPositionTypeOrgOperatorMapper;
import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPosTypeOrgOperatorProcessingServiceTest {

  @Mock
  private DebtPosTypeOrgOperatorsErrorsArchiverService errorsArchiverServiceMock;
  @Mock
  private Path workingDirectory;
  @Mock
  private DebtPositionTypeOrgOperatorMapper mapperMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepositoryMock;
  @Mock
  private DebtPositionTypeOrgService debtPositionTypeOrgServiceMock;
  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private CsvService csvServiceMock;

  private DebtPosTypeOrgOperatorProcessingService service;

  @BeforeEach
  void setUp() {
    service = new DebtPosTypeOrgOperatorProcessingService(errorsArchiverServiceMock,
      debtPositionTypeOrgOperatorsRepositoryMock,
      organizationServiceMock,
      debtPositionTypeOrgServiceMock,
      authnServiceMock,
      csvServiceMock
      );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      errorsArchiverServiceMock,
      debtPositionTypeOrgOperatorsRepositoryMock,
      organizationServiceMock,
      debtPositionTypeOrgServiceMock,
      authnServiceMock,
      csvServiceMock);
  }

  @Test
  void processOperatorDebtPosTypeOrgFileThrowsIfNotCsv() {
    Uploads uploads = new Uploads();
    Path file = Path.of("file.txt");
    assertThrows(IllegalArgumentException.class, () -> service.processOperatorDebtPosTypeOrgFile(file, uploads));
  }

  @Test
  void processOperatorDebtPosTypeOrgFileReturnsResultFromCsvService() throws Exception {
    Uploads uploads = new Uploads();
    Path file = Path.of("file.csv");
    DebtPositionTypeOrgOperatorMigrationFileResult expectedResult = new DebtPositionTypeOrgOperatorMigrationFileResult();
    when(csvServiceMock.readCsv(eq(file), eq(DebtPositionTypeOrgOperatorMigrationFileDTO.class), any())).thenReturn(expectedResult);
    DebtPositionTypeOrgOperatorMigrationFileResult result = service.processOperatorDebtPosTypeOrgFile(file, uploads);
    assertSame(expectedResult, result);
    verify(csvServiceMock).readCsv(eq(file), eq(DebtPositionTypeOrgOperatorMigrationFileDTO.class), any());
  }

  @Test
  void processOperatorDebtPosTypeOrgFileThrowsMigrationFileProcessingExceptionOnError() throws Exception {
    Uploads uploads = new Uploads();
    Path file = Path.of("file.csv");
    when(csvServiceMock.readCsv(eq(file), eq(DebtPositionTypeOrgOperatorMigrationFileDTO.class), any())).thenThrow(new RuntimeException("fail"));
    MigrationFileProcessingException ex = assertThrows(MigrationFileProcessingException.class, () -> service.processOperatorDebtPosTypeOrgFile(file, uploads));
    assertTrue(ex.getMessage().contains("Error processing file"));
  }


  @Test
  void consumeRowReturnsFalseAndAddsErrorIfDuplicate() {

    DebtPositionTypeOrgOperators existingDebtPosTypeOrgOperator = DebtPositionTypeOrgOperators.builder()
      .organizationId(0L)
      .debtPositionTypeOrgCode("CODE")
      .build();

    DebtPositionTypeOrgOperatorMigrationFileDTO dto = mock(DebtPositionTypeOrgOperatorMigrationFileDTO.class);
    when(dto.getOrgIpaCode()).thenReturn("IPA");
    when(dto.getDebtPositionTypeOrgCode()).thenReturn("CODE");
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    when(organizationServiceMock.getOrganizationByIpaCode(any(), any())).thenReturn(Optional.of(new Organization().organizationId(0L)));
    when(debtPositionTypeOrgOperatorsRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgCode(0L, "CODE"))
        .thenReturn(Optional.of(existingDebtPosTypeOrgOperator));
    when(debtPositionTypeOrgServiceMock.getDebtPositionTypeOrgByCodeAndOrgId(anyString(), anyLong(), any()))
        .thenReturn(Optional.of(new DebtPositionTypeOrg()));
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new java.util.ArrayList<>();
    boolean consumed = service.consumeRow(1, dto, result, errorList, new Uploads());
    assertFalse(consumed);
    assertFalse(errorList.isEmpty());
    assertEquals("OPERATOR_DEBT_POS_TYPE_ORG_ALREADY_EXISTS", errorList.getFirst().getErrorCode());
  }

  @Test
  void consumeRowReturnsTrueAndSavesEntityIfNotDuplicate() {
    DebtPositionTypeOrgOperatorMigrationFileDTO dto = mock(DebtPositionTypeOrgOperatorMigrationFileDTO.class);
    when(dto.getOrgIpaCode()).thenReturn("IPA");
    when(dto.getDebtPositionTypeOrgCode()).thenReturn("CODE");
    when(organizationServiceMock.getOrganizationByIpaCode(anyString(), anyString())).thenReturn(Optional.of(new Organization().organizationId(1L)));
    when(debtPositionTypeOrgOperatorsRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgCode(1L, "CODE")).thenReturn(Optional.empty());
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    DebtPositionTypeOrg debtTypeOrgDTO = new DebtPositionTypeOrg();
    debtTypeOrgDTO.organizationId(2L);
    when(debtPositionTypeOrgServiceMock.getDebtPositionTypeOrgByCodeAndOrgId(anyString(), anyLong(), any())).thenReturn(Optional.of(debtTypeOrgDTO));
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new java.util.ArrayList<>();
    boolean consumed = service.consumeRow(1, dto, result, errorList, new Uploads());
    assertTrue(consumed);
    verify(debtPositionTypeOrgOperatorsRepositoryMock).findByOrganizationIdAndDebtPositionTypeOrgCode(1L, "CODE");
    verify(organizationServiceMock).getOrganizationByIpaCode(anyString(), anyString());
    verify(debtPositionTypeOrgServiceMock).getDebtPositionTypeOrgByCodeAndOrgId(anyString(), anyLong(), any());
    verify(debtPositionTypeOrgOperatorsRepositoryMock).save(any(DebtPositionTypeOrgOperators.class));
  }

  @Test
  void consumeRowReturnsFalseAndAddsErrorIfOrganizationNotFound() {
    DebtPositionTypeOrgOperatorMigrationFileDTO dto = mock(DebtPositionTypeOrgOperatorMigrationFileDTO.class);
    when(dto.getOrgIpaCode()).thenReturn("IPA");
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    when(organizationServiceMock.getOrganizationByIpaCode(any(), any())).thenReturn(Optional.empty());
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new java.util.ArrayList<>();
    boolean consumed = service.consumeRow(1, dto, result, errorList, new Uploads());
    assertFalse(consumed);
    assertFalse(errorList.isEmpty());
    assertEquals("PROCESS_EXCEPTION", errorList.getFirst().getErrorCode());
  }

  @Test
  void consumeRowReturnsFalseAndAddsErrorIfDebtPositionTypeOrgNotFound() {
    DebtPositionTypeOrgOperatorMigrationFileDTO dto = mock(DebtPositionTypeOrgOperatorMigrationFileDTO.class);
    when(dto.getOrgIpaCode()).thenReturn("IPA");
    when(dto.getDebtPositionTypeOrgCode()).thenReturn("CODE");
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    when(organizationServiceMock.getOrganizationByIpaCode(any(), any())).thenReturn(Optional.of(new Organization().organizationId(1L)));
    when(debtPositionTypeOrgServiceMock.getDebtPositionTypeOrgByCodeAndOrgId(anyString(), anyLong(), any())).thenReturn(Optional.empty());
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new java.util.ArrayList<>();
    boolean consumed = service.consumeRow(1, dto, result, errorList, new Uploads());
    assertFalse(consumed);
    assertFalse(errorList.isEmpty());
    assertEquals("PROCESS_EXCEPTION", errorList.getFirst().getErrorCode());
  }

  @Test
  void consumeRowReturnsFalseAndAddsErrorIfContextIsNotUploads() {
    DebtPositionTypeOrgOperatorMigrationFileDTO dto = mock(DebtPositionTypeOrgOperatorMigrationFileDTO.class);
    when(dto.getOrgIpaCode()).thenReturn("IPA");
    when(dto.getDebtPositionTypeOrgCode()).thenReturn("CODE");
    when(authnServiceMock.getAccessToken()).thenReturn("token");
    when(organizationServiceMock.getOrganizationByIpaCode(any(), any())).thenReturn(Optional.of(new Organization().organizationId(1L)));
    when(debtPositionTypeOrgServiceMock.getDebtPositionTypeOrgByCodeAndOrgId(anyString(), anyLong(), any())).thenReturn(Optional.empty());
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new java.util.ArrayList<>();
    boolean consumed = service.consumeRow(1, dto, result, errorList, new Object());
    assertFalse(consumed);
    assertFalse(errorList.isEmpty());
    assertEquals("PROCESS_EXCEPTION", errorList.getFirst().getErrorCode());
  }

  @Test
  void setErrorListAndSetNumTotalRowsWork() {
    DebtPositionTypeOrgOperatorMigrationFileResult result = new DebtPositionTypeOrgOperatorMigrationFileResult();
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = List.of(new DebtPositionTypeOrgOperatorErrorDTO());
    service.setErrorList(result, errorList);
    assertEquals(errorList, result.getErrorList());
    service.setNumTotalRows(result, 5);
    assertEquals(5, result.getNumTotalRows());
  }

}
