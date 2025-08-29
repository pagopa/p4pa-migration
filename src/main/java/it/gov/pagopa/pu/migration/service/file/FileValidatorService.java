package it.gov.pagopa.pu.migration.service.file;

import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileValidatorService {

  public void validateMultipartFile(MultipartFile multipartFile) {
    if( multipartFile == null){
      log.debug("Invalid file");
      throw new InvalidFileException("Invalid file");
    }
    String filename = StringUtils.defaultString(multipartFile.getOriginalFilename());
    validateFilename(filename);
  }

  public static void validateFilename(String filename) {
    if(Stream.of("..", "\\", "/").anyMatch(filename::contains)){
      log.debug("Invalid filename");
      throw new InvalidFileException("Invalid filename");
    }
  }

  /**
   * Validates that the file exists and is a regular file.
   *
   * @param filePath the path to the file.
   * @throws InvalidFileException if the file does not exist or is not a regular file.
   */
  public void validateFile(Path filePath) {
    if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
      throw new InvalidFileException("File not found: " + filePath);
    }
  }

  /**
   * Checks if the specified file is a valid archive by analyzing its signature.
   *
   * @param zipFilePath the path to the ZIP file to check.
   * @return true if the file is a valid ZIP archive; false otherwise.
   * @throws InvalidFileException if the file is not a valid archive.
   */
  public boolean isArchive(Path zipFilePath) {
    try (RandomAccessFile raf = new RandomAccessFile(zipFilePath.toFile(), "r")) {
      int fileSignature = raf.readInt();
      return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    } catch (IOException e) {
      throw new InvalidFileException("Invalid zip file");
    }
  }

  /**
   * Checks if the specified file is a valid ZIP file by checking file extension.
   *
   * @param zipFilePath the path to the ZIP file to check.
   * @return true if the file is a valid ZIP archive; false otherwise.
   */
  public boolean isZipFileByExtension(Path zipFilePath) {
    return zipFilePath.getFileName().toString().toLowerCase().endsWith(".zip");
  }

}
