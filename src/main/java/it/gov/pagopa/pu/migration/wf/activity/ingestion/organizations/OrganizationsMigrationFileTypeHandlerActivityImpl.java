package it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.wf.exception.InvalidIngestionFileException;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
@ActivityImpl(taskQueues = WfConstants.TASK_QUEUE_MIGRATION)
@Slf4j
public class OrganizationsMigrationFileTypeHandlerActivityImpl extends BaseMigrationFileTypeHandlerActivity<MigrationFileResult> implements OrganizationsMigrationFileTypeHandlerActivity {

  private final FileShareService fileShareService;
  private final AuthnService authnService;
  private final UploadsRepository uploadsRepository;

  public OrganizationsMigrationFileTypeHandlerActivityImpl(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService,
    FileShareService fileShareService, AuthnService authnService) {
    super(uploadsRepository, fileRetrieverService, fileArchiverService);
    this.fileShareService = fileShareService;
    this.authnService = authnService;
    this.uploadsRepository = uploadsRepository;
  }

  @Override
  protected MigrationFileTypeEnum getHandledMigrationFileType() {
    return MigrationFileTypeEnum.ORGANIZATIONS;
  }

  @Override
  protected MigrationFileResult handleRetrievedFiles(List<Path> retrievedFiles, Uploads ingestionFlowFileDTO) {
    try {
      retrievedFiles.forEach(file -> {
        log.info("Processing unzipped file: {}", file);
        Uploads fileToProcess = Uploads.builder()
          .organizationId(ingestionFlowFileDTO.getOrganizationId())
          .filePathName(file.getParent().toString())
          .fileName(file.getFileName().toString())
          .fileSize(file.spliterator().estimateSize())
          .fileType(ingestionFlowFileDTO.getFileType())
          .status(UploadsStatusEnum.UPLOADED)
          .build();

        fileShareService.uploadIngestionFlowFile(
          ingestionFlowFileDTO.getOrganizationId(),
          IngestionFlowFileType.valueOf(ingestionFlowFileDTO.getFileType().name()),
          new FileSystemResource(file.toFile()),
          authnService.getAccessToken());

        uploadsRepository.save(fileToProcess);

      });
    } catch (Exception e) {
      log.error("Error processing file {}: {}", ingestionFlowFileDTO.getFileName(), e.getMessage(), e);
      throw new InvalidIngestionFileException(String.format("Error processing file %s: %s", ingestionFlowFileDTO.getFileName(), e.getMessage()));
    }
    return new MigrationFileResult();
  }
}
