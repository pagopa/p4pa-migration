package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorProcessingService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorsErrorsArchiverService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity {

  private final DebtPosTypeOrgOperatorProcessingService debtPosTypeOrgOperatorProcessingService;
  private final DebtPosTypeOrgOperatorsErrorsArchiverService errorsArchiverService;

  public DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService,
    AuthnService authnService,
    OrganizationService organizationService,
    ZipFileService zipFileService,
    DebtPosTypeOrgOperatorProcessingService debtPosTypeOrgOperatorProcessingService,
    DebtPosTypeOrgOperatorsErrorsArchiverService errorsArchiverService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService, fileShareService, authnService, organizationService, zipFileService);

    this.debtPosTypeOrgOperatorProcessingService = debtPosTypeOrgOperatorProcessingService;
    this.errorsArchiverService = errorsArchiverService;
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
    int numTotalrows = 0;
    int numCorrectlyProcessedRows = 0;


    final List<String> unsuccessfulParsedFiles = new ArrayList<>();

    for (Path file : retrievedFiles) {
      log.info("Processing file: {}", file);
      try {
        DebtPositionTypeOrgOperatorMigrationFileResult result = debtPosTypeOrgOperatorProcessingService.processOperatorDebtPosTypeOrgFile(file, upload);
        numTotalFiles++;
        numTotalrows += result.getNumTotalRows();
        if (result.getNumCorrectlyProcessedRows() > 0) {
          numCorrectlyProcessedFiles++;
          numCorrectlyProcessedRows += result.getNumCorrectlyProcessedRows();
        }
      } catch (Exception e) {
        log.error("Error processing file {}: {}", file, e.getMessage(), e);
        unsuccessfulParsedFiles.add(file.getFileName() + ":" + e.getMessage());
      }
    }

    String discardedFileName = errorsArchiverService.archiveErrorFiles(retrievedFiles.getFirst().getParent(), upload);
    String errorDescription = buildErrorDescription(unsuccessfulParsedFiles, discardedFileName);

    return MigrationFileResult.builder()
      .fileSize(upload.getFileSize())
      .numTotalFiles(numTotalFiles)
      .numCorrectlyProcessedFiles(numCorrectlyProcessedFiles)
      .discardedFileName(discardedFileName)
      .errorDescription(errorDescription)
      .numTotalRowsArchive(numTotalrows)
      .numCorrectlyProcessedTotalRowsArchive(numCorrectlyProcessedRows)
      .build();
  }


  private static String buildErrorDescription(List<String> unsuccessfulParsedFiles, String discardsFileName) {
    String errorDescription = null;
    if (!unsuccessfulParsedFiles.isEmpty() || discardsFileName !=null) {
      errorDescription = "There were some errors during Debt position type org operators migration.";
      if(discardsFileName !=null){
        errorDescription += " Please check error file.";
      }
      if(!unsuccessfulParsedFiles.isEmpty()){
        errorDescription += "\n" + String.join("\n", unsuccessfulParsedFiles);
      }
    }
    return errorDescription;
  }


}
