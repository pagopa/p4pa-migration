package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorProcessingService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity {

  private final DebtPosTypeOrgOperatorProcessingService debtPosTypeOrgOperatorProcessingService;

  public DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl(
          UploadsRepository uploadsRepository,
          MigrationFileRetrieverService fileRetrieverService,
          FileArchiverService fileArchiverService,
          FileShareService fileShareService,
          AuthnService authnService,
          OrganizationSearchClient organizationSearchClient,
          ZipFileService zipFileService,
          DebtPosTypeOrgOperatorProcessingService debtPosTypeOrgOperatorProcessingService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService, fileShareService, authnService, organizationSearchClient, zipFileService);

    this.debtPosTypeOrgOperatorProcessingService = debtPosTypeOrgOperatorProcessingService;
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS;
  }

  @Override
  protected MigrationFileResult handleRetrievedFiles(List<Path> retrievedFiles, Uploads upload) {
    if (retrievedFiles == null || retrievedFiles.isEmpty()) {
      throw new InvalidMigrationFileException("No file found in the uploaded archive");
    }

    int numTotalFiles = 0;
    int numCorrectlyProcessedFiles = 0;

    for (Path file : retrievedFiles) {
      log.info("Processing file: {}", file);
      try {
        DebtPositionTypeOrgOperatorMigrationFileResult result = debtPosTypeOrgOperatorProcessingService.processOperatorDebtPosTypeOrgFile(file, upload);
        numTotalFiles++;
        if (result.getNumCorrectlyProcessedRows() > 0) {
          numCorrectlyProcessedFiles++;
        }
      } catch (Exception e) {
        log.error("Error processing file {}: {}", file, e.getMessage(), e);
      }
    }

    return MigrationFileResult.builder()
      .fileSize(upload.getFileSize())
      .numTotalFiles(numTotalFiles)
      .numCorrectlyProcessedFiles(numCorrectlyProcessedFiles)
      .build();
  }

}
