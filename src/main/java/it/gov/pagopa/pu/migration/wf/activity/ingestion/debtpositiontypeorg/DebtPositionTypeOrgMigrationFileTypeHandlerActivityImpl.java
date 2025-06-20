package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorg;

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
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class DebtPositionTypeOrgMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements DebtPositionTypeOrgMigrationFileTypeHandlerActivity {

  public DebtPositionTypeOrgMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService,
    AuthnService authnService,
    OrganizationService organizationService,
    ZipFileService zipFileService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService, fileShareService, authnService, organizationService, zipFileService);
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG;
  }

  @Override
  protected MigrationFileResult handleRetrievedFiles(List<Path> retrievedFiles, Uploads upload) {
    return handleFilesUpload(
      retrievedFiles,
      upload,
      IngestionFlowFileType.DEBT_POSITIONS_TYPE_ORG,
      IngestionFlowFile.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE_ORG
    );
  }
}
