package it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.debtposition.DebtPositionTypeOrgService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorErrorDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.MigrationFileProcessingException;
import it.gov.pagopa.pu.migration.wf.mapper.DebtPositionTypeOrgOperatorMapper;
import it.gov.pagopa.pu.migration.wf.service.ingestion.ErrorArchiverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Lazy
@Slf4j
public class DebtPosTypeOrgOperatorProcessingService extends MigrationProcessingService<DebtPositionTypeOrgOperatorMigrationFileDTO, DebtPositionTypeOrgOperatorMigrationFileResult, DebtPositionTypeOrgOperatorErrorDTO> {

  private final DebtPositionTypeOrgOperatorsRepository repository;
  private final OrganizationService organizationService;
  private final DebtPositionTypeOrgService debtPositionTypeOrgService;
  private final AuthnService authnService;
  private final DebtPosTypeOrgOperatorsErrorsArchiverService debtPosTypeOrgOperatorsErrorsArchiverService;
  private final CsvService csvService;
  private final DebtPositionTypeOrgOperatorMapper debtPositionTypeOrgOperatorMapper;

  public DebtPosTypeOrgOperatorProcessingService(
    DebtPosTypeOrgOperatorsErrorsArchiverService debtPosTypeOrgOperatorsErrorsArchiverService,
    DebtPositionTypeOrgOperatorsRepository repository,
    OrganizationService organizationService,
    DebtPositionTypeOrgService debtPositionTypeOrgService,
    AuthnService authnService,
    CsvService csvService,
    ErrorArchiverService<DebtPositionTypeOrgOperatorErrorDTO> errorArchiverService, DebtPositionTypeOrgOperatorMapper debtPositionTypeOrgOperatorMapper) {
    super(errorArchiverService);
    this.debtPosTypeOrgOperatorsErrorsArchiverService = debtPosTypeOrgOperatorsErrorsArchiverService;
    this.repository = repository;
    this.organizationService = organizationService;
    this.debtPositionTypeOrgService = debtPositionTypeOrgService;
    this.authnService = authnService;
    this.csvService = csvService;
    this.debtPositionTypeOrgOperatorMapper = debtPositionTypeOrgOperatorMapper;
  }

  @Override
  protected ErrorArchiverService<DebtPositionTypeOrgOperatorErrorDTO> getErrorArchiverService() {
    return debtPosTypeOrgOperatorsErrorsArchiverService;
  }

  public DebtPositionTypeOrgOperatorMigrationFileResult processOperatorDebtPosTypeOrgFile(
    Path file,
    Uploads uploads) {
    if (!file.getFileName().toString().toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("File does not have .csv extension: " + file);
    }
    Path workingDirectory = file.getParent();
    try {
      return csvService.readCsv(file,
        DebtPositionTypeOrgOperatorMigrationFileDTO.class,
        (csvIterator, readerException) -> processOperatorDebtPosTypeOrg(csvIterator, readerException, uploads, workingDirectory, file.getFileName().toString()));
    } catch (Exception e) {
      log.error("Error processing file {}: {}", file, e.getMessage(), e);
      throw new MigrationFileProcessingException(String.format("Error processing file %s: %s", file, e.getMessage()));
    }
  }

  public DebtPositionTypeOrgOperatorMigrationFileResult processOperatorDebtPosTypeOrg(
    Iterator<DebtPositionTypeOrgOperatorMigrationFileDTO> iterator,
    List<CsvException> readerException,
    Uploads uploads,
    Path workingDirectory,
    String fileName) {
    List<DebtPositionTypeOrgOperatorErrorDTO> errorList = new ArrayList<>();
    DebtPositionTypeOrgOperatorMigrationFileResult migrationFileResult = new DebtPositionTypeOrgOperatorMigrationFileResult();
    migrationFileResult.setOrganizationId(uploads.getOrganizationId());
    this.process(
      iterator,
      readerException,
      migrationFileResult,
      uploads,
      errorList,
      this::buildErrorDto,
      workingDirectory,
      fileName
    );
    return migrationFileResult;
  }

  @Override
  protected boolean consumeRow(long lineNumber, DebtPositionTypeOrgOperatorMigrationFileDTO dto, DebtPositionTypeOrgOperatorMigrationFileResult migrationFileResult, List<DebtPositionTypeOrgOperatorErrorDTO> errorList, Uploads upload) {
    String fileName = upload.getFileName();
    try {
      // Retrieve organizationId and debtPositionTypeOrgId
      Long organizationId = organizationService.getOrganizationByIpaCode(dto.getOrgIpaCode(), authnService.getAccessToken())
        .orElseThrow(() -> new IllegalArgumentException("Organization with IPA code " + dto.getOrgIpaCode() + " not found"))
        .getOrganizationId();
      Long debtPositionTypeOrgId = debtPositionTypeOrgService.getDebtPositionTypeOrgByCodeAndOrgId(
          dto.getDebtPositionTypeOrgCode(), organizationId, authnService.getAccessToken())
        .orElseThrow(() -> new IllegalArgumentException("DebtPositionTypeOrg with code " + dto.getDebtPositionTypeOrgCode() + " not found for orgId " + organizationId))
        .getDebtPositionTypeOrgId();

      // Check for duplicates
      DebtPositionTypeOrgOperators entity = debtPositionTypeOrgOperatorMapper.mapToOperators(dto, debtPositionTypeOrgId, organizationId);
      if (entity == null) {
        errorList.add(buildErrorDto(
          fileName,
          lineNumber,
          "MAPPING_ERROR",
          "Mapping to DebtPositionTypeOrgOperators returned null"
        ));
        return false;
      }

      Optional<DebtPositionTypeOrgOperators> existingOrg = repository.findFirstByOrganizationIdAndDebtPositionTypeOrgCodeAndCfOperatorHash(
        organizationId, dto.getDebtPositionTypeOrgCode(), entity.getCfOperatorHash());
      if (existingOrg.isPresent()) {
        errorList.add(buildErrorDto(
          fileName,
          lineNumber,
          "OPERATOR_DEBT_POS_TYPE_ORG_ALREADY_EXISTS",
          "Operator debt position type org already exists"
        ));
        return false;
      }

      DebtPositionTypeOrgOperators savedEntity = repository.save(entity);
      log.info("Saved OperatorsDebtPositionTypeOrg: orgIpaCode={}, debtPositionTypeOrgCode={}, organizationId={}, debtPositionTypeOrgId={}",
        dto.getOrgIpaCode(), dto.getDebtPositionTypeOrgCode(), savedEntity.getOrganizationId(), savedEntity.getDebtPositionTypeOrgId());
      return true;

    } catch (Exception e) {
      log.error("Error processing operator debt position type org with ipa code {} and debt pos type org code {}: {}",
        dto.getOrgIpaCode(), dto.getDebtPositionTypeOrgCode(), e.getMessage());
      errorList.add(buildErrorDto(
        fileName,
        lineNumber,
        "PROCESS_EXCEPTION",
        e.getMessage()
      ));
      return false;
    }
  }

  @Override
  protected void setNumTotalRows(DebtPositionTypeOrgOperatorMigrationFileResult result, long numTotalRows) {
    result.setNumTotalRows(numTotalRows);
  }

  @Override
  protected void setNumCorrectlyProcessedRows(DebtPositionTypeOrgOperatorMigrationFileResult result, long numCorrectlyProcessedRows) {
    result.setNumCorrectlyProcessedRows(numCorrectlyProcessedRows);
  }

  @Override
  protected void setErrorDescription(DebtPositionTypeOrgOperatorMigrationFileResult result, String errorDescription) {
    result.setErrorDescription(errorDescription);
  }

  @Override
  protected void setDiscardedFileName(DebtPositionTypeOrgOperatorMigrationFileResult result, String discardedFileName) {
    result.setDiscardedFileName(discardedFileName);
  }

  protected DebtPositionTypeOrgOperatorErrorDTO buildErrorDto(String fileName, long lineNumber, String errorCode, String message) {
    return DebtPositionTypeOrgOperatorErrorDTO.builder()
      .fileName(fileName)
      .rowNumber(lineNumber)
      .errorCode(errorCode)
      .errorMessage(message)
      .build();
  }
}
