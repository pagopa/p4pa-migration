package it.gov.pagopa.pu.migration.service.file;

import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileValidatorServiceTest {
  @TempDir
  private Path tempDir;

  private FileValidatorService service;

  @BeforeEach
  void setUp() {
    service = new FileValidatorService();
  }

  @Test
  void givenValidFileExtensionWhenValidateFileThenOk() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    service.validateMultipartFile(file);
  }

  @Test
  void givenNoFileWhenValidateFileThenInvalidMultipartFileException() {
    try {
      service.validateMultipartFile(null);
      Assertions.fail("Expected InvalidFileException");
    } catch (InvalidFileException e) {
      //do nothing
    }
  }

  @Test
  void givenInvalidFilenameWhenValidateFileThenInvalidMultipartFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "../test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try {
      service.validateMultipartFile(file);
      Assertions.fail("Expected InvalidFileException");
    } catch (InvalidFileException e) {
      //do nothing
    }
  }

  @Test
  void validateFile_validMultipartFile_doesNotThrowException() throws IOException {
    Path validFile = Files.createFile(tempDir.resolve("validFile.txt"));

    assertDoesNotThrow(() -> service.validateFile(validFile), "Expected file is valid");
  }

  @Test
  void validateFile_nonExistentFile_throwsInvalidMultipartFileException() {
    Path nonExistentFile = tempDir.resolve("nonExistentFile.txt");

    assertThrows(InvalidFileException.class,
      () -> service.validateFile(nonExistentFile), "Expected file not exist"
    );
  }

  @Test
  void validateFile_directoryInsteadOfFile_throwsInvalidMultipartFileException() {
    Path directory = tempDir.resolve("directory");
    assertTrue(directory.toFile().mkdir());

    assertThrows(InvalidFileException.class,
      () -> service.validateFile(directory), "Expected file is not a regular file"
    );
  }

  @Test
  void testIsArchiveWithValidZipFile() throws IOException {
    Path validZip = tempDir.resolve("valid.zip");
    Files.write(validZip, new byte[]{0x50, 0x4B, 0x03, 0x04});

    assertTrue(service.isArchive(validZip), "Expected file to be recognized as a valid ZIP archive");
  }

  @Test
  void testIsArchiveWithInvalidZipFile() throws IOException {
    Path invalidZip = tempDir.resolve("invalid.zip");
    Files.write(invalidZip, new byte[]{0x00, 0x00, 0x00, 0x00});

    assertFalse(service.isArchive(invalidZip), "Expected file to not be recognized as a valid ZIP archive");
  }

  @Test
  void testIsArchiveWithEmptyFile() throws IOException {
    Path emptyFile = tempDir.resolve("empty.zip");
    Files.createFile(emptyFile);

    assertThrows(InvalidFileException.class,
      () -> service.isArchive(emptyFile), "Expected InvalidFileException for an empty file");
  }

  @Test
  void givenNotZipFileWhenIsZipFileByExtensionThenReturnFalse() {
    Path notZipFile = Path.of("notZipFile.txt");
    assertFalse(service.isZipFileByExtension(notZipFile), "Expected file to not be recognized as a ZIP file");
  }

  @Test
  void givenZipFileWhenIsZipFileByExtensionThenReturnTrue() {
    Path zipFile = Path.of("zipFile.zip");
    assertTrue(service.isZipFileByExtension(zipFile), "Expected file to be recognized as a ZIP file");
  }
}
