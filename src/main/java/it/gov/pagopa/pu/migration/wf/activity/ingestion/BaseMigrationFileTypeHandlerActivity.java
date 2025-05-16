package it.gov.pagopa.pu.migration.wf.activity.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.MigrationFileTypeNotSupportedException;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public abstract class BaseMigrationFileTypeHandlerActivity<T extends MigrationFileResult> {

  private final UploadsRepository uploadsRepository;
	private final MigrationFileRetrieverService fileRetrieverService;
	private final FileArchiverService fileArchiverService;

	protected BaseMigrationFileTypeHandlerActivity(
    UploadsRepository uploadsRepository,
    MigrationFileRetrieverService fileRetrieverService,
    FileArchiverService fileArchiverService
  ) {
    this.uploadsRepository = uploadsRepository;
		this.fileRetrieverService = fileRetrieverService;
		this.fileArchiverService = fileArchiverService;
	}

  /** It will:
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
	 * @throws UploadNotFoundException if the record is not found
	 * @throws IllegalArgumentException if the flow file type is invalid
	 */
	private Uploads findUploadRecord(Long uploadId) {
		Uploads upload = uploadsRepository.findById(uploadId)
			.orElseThrow(() -> new UploadNotFoundException("Cannot found upload having id: "+ uploadId));

		if (!(getHandledMigrationFileType()).equals(upload.getFileType())) {
			throw new MigrationFileTypeNotSupportedException("invalid migration file type: " + upload.getFileType() + " expected " + getHandledMigrationFileType());
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
					log.warn("Error occurred while deleting file: " + pathToDelete + " " +e.getMessage());
				}
			}
		}
	}

	/** The {@link MigrationFileTypeEnum} supported */
	protected abstract MigrationFileTypeEnum getHandledMigrationFileType();

	/** It will process retrieve files */
	protected abstract T handleRetrievedFiles(List<Path> retrievedFiles, Uploads ingestionFlowFileDTO);
}
