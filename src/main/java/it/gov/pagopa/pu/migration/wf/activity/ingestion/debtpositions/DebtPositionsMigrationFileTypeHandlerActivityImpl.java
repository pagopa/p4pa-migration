package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositions;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionErrorDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition.DebtPositionProcessingService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class DebtPositionsMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements DebtPositionsMigrationFileTypeHandlerActivity {

  private final DebtPositionProcessingService debtPositionProcessingService;

  public DebtPositionsMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService,
    AuthnService authnService,
    OrganizationService organizationService,
    ZipFileService zipFileService, DebtPositionProcessingService debtPositionProcessingService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService, fileShareService, authnService, organizationService, zipFileService);
    this.debtPositionProcessingService = debtPositionProcessingService;
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.DEBT_POSITIONS;
  }

  @Override
  protected MigrationFileResult handleRetrievedFiles(List<Path> retrievedFiles, Uploads upload) {
    if (retrievedFiles == null || retrievedFiles.isEmpty()) {
      throw new InvalidMigrationFileException("No file found in the uploaded archive");
    }
    final List<DebtPositionErrorDTO> errorList = new ArrayList<>();
    DebtPositionMigrationFileResult result = debtPositionProcessingService.readAndParseRows(retrievedFiles, errorList);
    return handleFilesUpload(
      result.getParsedFiles(),
      upload,
      IngestionFlowFileType.DP_INSTALLMENTS
    );
  }
}
