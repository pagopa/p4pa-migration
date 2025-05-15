package it.gov.pagopa.pu.migration.wf.service.ingestion;

import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service class responsible for handling encrypted ingestion files,
 * including decryption, validation, and extraction of ZIP files.
 */
@Lazy
@Slf4j
@Service
public class MigrationFileRetrieverService {

    /**
     * The temporary directory used for working process.
     */
    private final Path tempDirectoryPath;

    private final FileStorerService fileStorerService;
    private final FileValidatorService fileValidatorService;
    private final ZipFileService zipFileService;

    public MigrationFileRetrieverService(
      @Value("${folders.tmp}") String tempFolder,
      FileStorerService fileStorerService,
      FileValidatorService fileValidatorService,
      ZipFileService zipFileService
    ) {
      this.tempDirectoryPath = Path.of(tempFolder);
      this.fileStorerService = fileStorerService;
      this.fileValidatorService = fileValidatorService;
        this.zipFileService = zipFileService;

        if (!Files.exists(tempDirectoryPath)) {
            throw new IllegalStateException("Temp folder doesn't exist: " + tempDirectoryPath);
        }
    }

    /**
     * Handles the setup process for an ingestion file by performing the following steps:
     * <ul>
     *     <li>Decrypts the file using AES encryption.</li>
     *     <li>Validates if the file is a valid ZIP archive.</li>
     *     <li>Extracts the contents of the ZIP file to a temporary directory, in case the file is zipped.</li>
     * </ul>
     *
     * @param sourcePath the relative path to the directory containing the file.
     * @param filename   the name of the file to process.
     * @return the path to the plain files (decrypted and, in case of archive file, unzipped).
     */
    public List<Path> retrieveAndUnzipFile(Long organizationId, Path sourcePath, String filename) {
        log.debug("Retrieving file: {}", filename);
        Path organizationFolder = fileStorerService.buildOrganizationBasePath(organizationId);
        Path encryptedFilePath = organizationFolder
                .resolve(sourcePath)
                .resolve(filename + AESUtils.CIPHER_EXTENSION);

        fileValidatorService.validateFile(encryptedFilePath);

        Path workingPath = tempDirectoryPath
                .resolve(String.valueOf(organizationId))
                .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
        try {
            Files.createDirectories(workingPath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create working directory: " + workingPath, e);
        }

        String filenameNoCipher = filename.replace(AESUtils.CIPHER_EXTENSION, "");
        Path decryptedFilePath = workingPath.resolve(filenameNoCipher);

        log.debug("Decrypting file: {}", encryptedFilePath);
        fileStorerService.decryptFile(encryptedFilePath.toFile(), decryptedFilePath.toFile());

        List<Path> plainFilePaths;
        if(fileValidatorService.isZipFileByExtension(decryptedFilePath)){
            log.debug("Validating ZIP file: {}", decryptedFilePath);
            fileValidatorService.isArchive(decryptedFilePath);

            log.debug("Unzipping files in : {}", decryptedFilePath);
            plainFilePaths = zipFileService.unzip(decryptedFilePath);
        } else {
            plainFilePaths = List.of(decryptedFilePath);
        }

        log.debug("File process completed successfully for: {}", filenameNoCipher);
        return plainFilePaths;
    }
}
