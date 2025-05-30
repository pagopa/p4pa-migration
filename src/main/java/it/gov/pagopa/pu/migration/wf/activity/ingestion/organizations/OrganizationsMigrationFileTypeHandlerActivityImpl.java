package it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class OrganizationsMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements OrganizationsMigrationFileTypeHandlerActivity {

  private final FileShareService fileShareService;
  private final AuthnService authnService;
  private final OrganizationSearchClient organizationSearchClient;

  public OrganizationsMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService, AuthnService authnService, OrganizationSearchClient organizationSearchClient) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService);
    this.fileShareService = fileShareService;
    this.authnService = authnService;
    this.organizationSearchClient = organizationSearchClient;
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.ORGANIZATIONS;
  }

  @Override
  protected MigrationFileResult handleRetrievedFiles(List<Path> retrievedFiles, Uploads upload) {
    if (retrievedFiles == null || retrievedFiles.isEmpty()) {
      throw new InvalidMigrationFileException("No file found in the uploaded archive");
    }

    Organization organization = organizationSearchClient.getByOrganizationId(
      upload.getOrganizationId(),
      authnService.getAccessToken()
    );

    List<IngestionFlowFile> filesUploaded = new ArrayList<>(retrievedFiles.size());
    for (Path file : retrievedFiles) {
      log.info("Processing unzipped file: {}", file);
      Long id = fileShareService.uploadIngestionFlowFile(
        upload.getOrganizationId(),
        IngestionFlowFileType.ORGANIZATIONS,
        new FileSystemResource(file.toFile()),
        authnService.getAccessToken(organization.getIpaCode())
      );
      filesUploaded.add(IngestionFlowFile.builder()
        .ingestionFlowFileId(id)
        .fileName(file.getFileName().toString())
        .fileSize(file.toFile().length())
        .ingestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.ORGANIZATIONS)
        .organizationId(upload.getOrganizationId())
        .operatorExternalId(upload.getUpdateOperatorExternalId())
        .filePathName(file.getFileName().toString())
        .status(IngestionFlowFileStatus.UPLOADED)
        .fileOrigin("MIGRATION")
        .build());
    }
    return MigrationFileResult.builder()
      .fileSize(upload.getFileSize())
      .numTotalFiles(retrievedFiles.size())
      .numCorrectlyProcessedFiles(filesUploaded.size())
      .ingestionFlowFiles(filesUploaded)
      .build();
  }
}
