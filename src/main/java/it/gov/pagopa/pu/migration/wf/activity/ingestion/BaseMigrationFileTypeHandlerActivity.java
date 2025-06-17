package it.gov.pagopa.pu.migration.wf.activity.ingestion;

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
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.exception.MigrationFileTypeNotSupportedException;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseMigrationFileTypeHandlerActivity<T extends MigrationFileResult> {

  private final UploadsRepository uploadsRepository;
  private final MigrationFileRetrieverService fileRetrieverService;
  private final FileArchiverService fileArchiverService;
  private final FileShareService fileShareService;
  private final AuthnService authnService;
  private final OrganizationSearchClient organizationSearchClient;
  private final ZipFileService zipFileService;

  /**
   * It will:
   * <ol>
   *   <li>Search the {@link Uploads} record</li>
   *   <li>Verify if it's type is supported by this Activity impl through the method {@link #getHandledMigrationFileType()}</li>
   *   <li>Retrieve and unzipping the file</li>
   *   <li>Call the {@link #handleRetrievedFiles(List, Uploads)} to demand to the Activity impl the handling of the extracted files</li>
   *   <li>Archive the input file</li>
   *   <li>Finally, it will delete unzipped files</li>
   * </ol>
   */
  public T processFile(Long uploadId) {
    log.info("Processing Upload {} using class {}", uploadId, getClass());
    List<Path> retrievedFiles = null;
    try {
      Uploads uploads = findUploadRecord(uploadId);

      retrievedFiles = retrieveFiles(uploads);

      T result = handleRetrievedFiles(retrievedFiles, uploads);

      fileArchiverService.archive(uploads);

      return result;
    } finally {
      deletion(retrievedFiles);
    }
  }

  /**
   * Retrieves the {@link Uploads} record for the given ID. If no record is found, throws
   * an {@link UploadNotFoundException}. Validates the flow file type before returning.
   *
   * @param uploadId the ID of the ingestion flow file to retrieve
   * @return the {@link Uploads} corresponding to the given ID
   * @throws UploadNotFoundException  if the record is not found
   * @throws IllegalArgumentException if the flow file type is invalid
   */
  private Uploads findUploadRecord(Long uploadId) {
    Uploads upload = uploadsRepository.findById(uploadId)
      .orElseThrow(() -> new UploadNotFoundException("Cannot found upload having id: " + uploadId));

    if (!(getHandledMigrationFileType()).equals(upload.getFileType())) {
      throw new MigrationFileTypeNotSupportedException("Invalid migration file type: " + upload.getFileType() + " expected " + getHandledMigrationFileType());
    }

    return upload;
  }

  /**
   * Retrieves the file associated with the provided {@link Uploads} by unzipping and
   * extracting it from the specified file path.
   *
   * @param upload the upload record containing file details
   * @return the extracted {@link List} from the ingestion flow
   */
  private List<Path> retrieveFiles(Uploads upload) {
    return fileRetrieverService
      .retrieveAndUnzipFile(upload.getOrganizationId(), Path.of(upload.getFilePathName()), upload.getFileName());
  }

  /**
   * Deletes the specified List of path if it is not null.
   *
   * @param pathsToDelete The list of path to delete.
   */
  private void deletion(List<Path> pathsToDelete) {
    if (pathsToDelete != null && !pathsToDelete.isEmpty()) {
      for (Path pathToDelete : pathsToDelete) {
        try {
          Files.delete(pathToDelete);
        } catch (IOException e) {
          log.warn("Error occurred while deleting file: " + pathToDelete + " " + e.getMessage());
        }
      }
    }
  }

  /**
   * The {@link MigrationFileTypeEnum} supported
   */
  protected abstract MigrationFileTypeEnum getHandledMigrationFileType();

  /**
   * It will process retrieve files
   */
  protected abstract T handleRetrievedFiles(List<Path> retrievedFiles, Uploads ingestionFlowFileDTO);

  /**
   * Protected method to handle the upload of extracted files, parameterized to eliminate duplication.
   */
  protected MigrationFileResult handleFilesUpload(
    List<Path> retrievedFiles,
    Uploads upload,
    IngestionFlowFileType ingestionFlowFileType,
    IngestionFlowFile.IngestionFlowFileTypeEnum ingestionFlowFileTypeEnum
  ) {
    if (retrievedFiles == null || retrievedFiles.isEmpty()) {
      throw new InvalidMigrationFileException("No file found in the uploaded archive");
    }

    List<IngestionFlowFile> filesUploaded = new ArrayList<>(retrievedFiles.size());
    for (Path file : retrievedFiles) {
      String fileName = file.getFileName().toString();
      String ipaCodeFile = extractIpaCodeFromFileName(fileName);

      Organization organization = organizationSearchClient.getByIpaCode(ipaCodeFile, authnService.getAccessToken());

      if (!organization.getBrokerId().equals(upload.getOrganizationId())) {
        throw new InvalidMigrationFileException("Organization whit ipa code " + ipaCodeFile + " is not associated to managed organizations." );
      }
      String zipName = Utilities.replaceFileExtension(fileName, ".zip");
      Path zipFilePath = file.getParent().resolve(zipName);
      File zippedFile = zipFileService.zipper(zipFilePath, List.of(file));
      log.info("Processing unzipped file: {}", file);
      Long id = fileShareService.uploadIngestionFlowFile(
        organization.getOrganizationId(),
        ingestionFlowFileType,
        new FileSystemResource(zippedFile),
        authnService.getAccessToken(ipaCodeFile)
      );
      filesUploaded.add(IngestionFlowFile.builder()
        .ingestionFlowFileId(id)
        .fileName(file.getFileName().toString())
        .fileSize(file.toFile().length())
        .ingestionFlowFileType(ingestionFlowFileTypeEnum)
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

  private String extractIpaCodeFromFileName(String fileName) {
    String[] parts = fileName.split("-");
    if (parts.length < 2) {
      throw new InvalidMigrationFileException("Invalid file name format: " + fileName);
    }
    return parts[0];
  }

}
