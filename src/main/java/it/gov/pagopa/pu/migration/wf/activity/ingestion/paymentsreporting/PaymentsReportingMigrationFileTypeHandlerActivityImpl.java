package it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentsreporting;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.utils.Utilities;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivity;
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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class PaymentsReportingMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements PaymentsReportingMigrationFileTypeHandlerActivity {

  private final FileShareService fileShareService;
  private final AuthnService authnService;
  private final OrganizationSearchClient organizationSearchClient;
  private final ZipFileService zipFileService;

  public PaymentsReportingMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService, AuthnService authnService, OrganizationSearchClient organizationSearchClient, ZipFileService zipFileService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService);
    this.fileShareService = fileShareService;
    this.authnService = authnService;
    this.organizationSearchClient = organizationSearchClient;
    this.zipFileService = zipFileService;
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.PAYMENTS_REPORTING;
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
      String fileName = file.getFileName().toString();
      String zipName = Utilities.replaceFileExtension(fileName, ".zip");
      Path zipFilePath = file.getParent().resolve(zipName);
      File zippedFile = zipFileService.zipper(zipFilePath, List.of(file));

      log.info("Processing unzipped file: {}", file);
      Long id = fileShareService.uploadIngestionFlowFile(
        upload.getOrganizationId(),
        IngestionFlowFileType.PAYMENTS_REPORTING,
        new FileSystemResource(zippedFile),
        authnService.getAccessToken(organization.getIpaCode())
      );
      filesUploaded.add(IngestionFlowFile.builder()
        .ingestionFlowFileId(id)
        .fileName(file.getFileName().toString())
        .fileSize(file.toFile().length())
        .ingestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING)
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
