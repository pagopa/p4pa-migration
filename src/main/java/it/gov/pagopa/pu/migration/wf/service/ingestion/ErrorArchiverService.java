package it.gov.pagopa.pu.migration.wf.service.ingestion;

import it.gov.pagopa.pu.migration.dto.ErrorFileDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.utils.Utilities;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * A base service for archiving error files.
 * This class provides common functionality for writing error data to CSV files and archiving them into ZIP archives.
 * It is designed to be extended by specific error archiver services for different types of error DTOs.
 *
 * @param <T> The type of the error DTO.
 */
@Slf4j
public abstract class ErrorArchiverService<T extends ErrorFileDTO> {

    private static final String ERRORFILE_PREFIX = "ERROR-";

    private final Path sharedDirectoryPath;
    private final String errorFolder;
    private final FileArchiverService fileArchiverService;
    private final CsvService csvService;

    protected ErrorArchiverService(
            String sharedFolder,
            String errorFolder,
            FileArchiverService fileArchiverService,
            CsvService csvService
    ) {
        this.sharedDirectoryPath = Path.of(sharedFolder);
        this.errorFolder = errorFolder;
        this.fileArchiverService = fileArchiverService;
        this.csvService = csvService;
    }


    protected abstract List<String[]> getHeaders();

    /**
     * Writes error data into a CSV file.
     *
     * @param workingDirectory  The directory where the file should be created.
     * @param upload The metadata of the upload file.
     * @param errorList         The list of errors to write.
     */
    public void writeErrors(Path workingDirectory, Uploads upload, List<T> errorList) {

        if(CollectionUtils.isEmpty(errorList)){
            return;
        }

        List<String[]> data = errorList.stream()
                .map(ErrorFileDTO::toCsvRow)
                .toList();

        try {
            String errorFileName = ERRORFILE_PREFIX + Utilities.replaceFileExtension(upload.getFileName(), ".csv");
            Path errorCsvFilePath = workingDirectory.resolve(errorFileName);

            csvService.createCsv(errorCsvFilePath, getHeaders(), data);
            log.info("Error CSV created: {}", errorCsvFilePath);

        } catch (IOException e) {
            throw new NotRetryableActivityException(e.getMessage());
        }
    }

    /**
     * Archives an error file to a specified target directory.
     * This method takes an error file and moves it to a target directory for archiving. It constructs
     * the original file path and the target directory path, then invokes the {@link FileArchiverService}
     * to perform the archiving operation.
     *
     * @param workingDirectory     the working directory where to search for error files to be archived. This file is moved from its original location to the target directory.
     * @param upload the upload file
     * @return the name of the archived error file (ZIP) if exists
     */
    public String archiveErrorFiles(Path workingDirectory, Uploads upload) {
        try {
            List<Path> errorFiles;
            try (Stream<Path> fileListStream = Files.list(workingDirectory)) {
                errorFiles = fileListStream
                        .filter(f -> f.getFileName().toString().startsWith(ERRORFILE_PREFIX))
                        .toList();
            }

            if (!errorFiles.isEmpty()) {

                Path targetDirectory = sharedDirectoryPath
                        .resolve(String.valueOf(upload.getOrganizationId()))
                        .resolve(upload.getFilePathName())
                        .resolve(errorFolder);

                String zipFileName = ERRORFILE_PREFIX + Utilities.replaceFileExtension(upload.getFileName(), ".zip");
                Path zipFile = workingDirectory.resolve(zipFileName);

                fileArchiverService.compressAndArchive(errorFiles, zipFile, targetDirectory);

                return zipFileName;
            } else {
                return null;
            }
        } catch (IOException e){
            log.error("Something gone wrong while trying to archive error file!", e);
            return null;
        }
    }
}
