package it.gov.pagopa.pu.migration.wf.service.ingestion;

import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationFileRetrieverServiceTest {

  private static final String TEMPORARY_PATH = "/tmp";

  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private FileValidatorService fileValidatorServiceMock;
  @Mock
  private ZipFileService zipFileServiceMock;

  private MigrationFileRetrieverService service;

  private Path zipFile;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void setup() throws IOException {
    FoldersPathsConfig foldersPathsConfig = new FoldersPathsConfig();
    FoldersPathsConfig.ProcessTargetSubFolders processTargetSubFolders = new FoldersPathsConfig.ProcessTargetSubFolders();
    processTargetSubFolders.setErrors("errors");
    foldersPathsConfig.setProcessTargetSubFolders(processTargetSubFolders);
    service = new MigrationFileRetrieverService(TEMPORARY_PATH, foldersPathsConfig, fileStorerServiceMock, fileValidatorServiceMock, zipFileServiceMock);
    zipFile = tempDir.resolve("encryptedFile.zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      addZipEntry(zos, "file1.txt", "This is the content of file1.");
      addZipEntry(zos, "file2.txt", "This is the content of file2.");
    }
  }

  @Test
  void testRetrieveFile_successfulFlow() {
    //Given
    Long organizationId = 0L;
    Path organizationPath = Path.of("shared", String.valueOf(organizationId));
    String filename = zipFile.getFileName().toString();
    Path sourcePath = zipFile.getParent();
    Path cipheredFilePath = organizationPath.resolve(sourcePath).resolve(filename + AESUtils.CIPHER_EXTENSION);
    Path workingPath = Path.of(TEMPORARY_PATH)
      .resolve(String.valueOf(organizationId))
      .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
    Path zipFileInWorkingDirectory = workingPath.resolve(filename);
    List<Path> unzippedPaths = List.of(workingPath.resolve("file1.txt"), workingPath.resolve("file2.txt"));

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationPath);
    doNothing().when(fileValidatorServiceMock).validateFile(cipheredFilePath);
    doReturn(true).when(fileValidatorServiceMock).isArchive(zipFileInWorkingDirectory);
    doReturn(true).when(fileValidatorServiceMock).isZipFileByExtension(zipFileInWorkingDirectory);
    when(zipFileServiceMock.unzip(zipFileInWorkingDirectory)).thenReturn(unzippedPaths);

    doNothing().when(fileStorerServiceMock)
      .decryptFile(cipheredFilePath.toFile(), zipFileInWorkingDirectory.toFile());

    // when
    List<Path> result = service.retrieveAndUnzipFile(organizationId, sourcePath, filename);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(path -> path.getFileName().toString().equals("file1.txt")));
    assertTrue(result.stream().anyMatch(path -> path.getFileName().toString().equals("file2.txt")));
    assertEquals(unzippedPaths, result);
  }

  @Test
  void testRetrieveFile_successfulFlow_notZipped() throws IOException {
    //Given
    Long organizationId = 0L;
    Path nonZippedFile = Files.createTempFile("testRetrieveFile_successfulFlow_notZipped", ".tmp");
    nonZippedFile.toFile().deleteOnExit();
    Path organizationPath = Path.of("shared", String.valueOf(organizationId));
    Path sourcePath = nonZippedFile.getParent();
    String filename = nonZippedFile.getFileName().toString();
    Path cipheredFilePath = organizationPath.resolve(sourcePath).resolve(filename + AESUtils.CIPHER_EXTENSION);
    Path workingPath = Path.of(TEMPORARY_PATH)
      .resolve(String.valueOf(organizationId))
      .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
    Path zipFileInWorkingDirectory = workingPath.resolve(filename);
    List<Path> unzippedPaths = List.of(workingPath.resolve(filename));

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationPath);
    doNothing().when(fileValidatorServiceMock).validateFile(cipheredFilePath);
    doReturn(false).when(fileValidatorServiceMock).isZipFileByExtension(zipFileInWorkingDirectory);

    doNothing().when(fileStorerServiceMock)
      .decryptFile(cipheredFilePath.toFile(), zipFileInWorkingDirectory.toFile());

    // when
    List<Path> result = service.retrieveAndUnzipFile(organizationId, sourcePath, filename);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(unzippedPaths, result);
  }

  @Test
  void testRetrieveFile_validationFails() {
    //Given
    Long organizationId = 0L;
    Path sourcePath = zipFile.getParent();
    String filename = zipFile.getFileName().toString();

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(sourcePath);
    doThrow(new InvalidFileException("File validation failed")).when(fileValidatorServiceMock).validateFile(sourcePath.resolve(filename + AESUtils.CIPHER_EXTENSION));

    //When & Then
    assertThrows(InvalidFileException.class,
      () -> service.retrieveAndUnzipFile(organizationId, sourcePath, filename), "File validation failed");
  }

  @Test
  void testRetrieveFile_zipValidationFails() {
    //Given
    Long organizationId = 0L;
    Path organizationPath = Path.of("shared", String.valueOf(organizationId));
    Path sourcePath = zipFile.getParent();
    String filename = zipFile.getFileName().toString();
    Path cipheredFilePath = organizationPath.resolve(sourcePath).resolve(filename + AESUtils.CIPHER_EXTENSION);
    Path workingPath = Path.of(TEMPORARY_PATH)
      .resolve(String.valueOf(organizationId))
      .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
    Path zipFileInWorkingDirectory = workingPath.resolve(filename);

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationPath);
    doNothing().when(fileValidatorServiceMock).validateFile(cipheredFilePath);
    doThrow(new InvalidFileException("ZIP validation failed")).when(fileValidatorServiceMock).isArchive(zipFileInWorkingDirectory);
    doReturn(true).when(fileValidatorServiceMock).isZipFileByExtension(zipFileInWorkingDirectory);

    doNothing().when(fileStorerServiceMock)
      .decryptFile(cipheredFilePath.toFile(), zipFileInWorkingDirectory.toFile());

    //When & Then
    assertThrows(InvalidFileException.class,
      () -> service.retrieveAndUnzipFile(organizationId, sourcePath, filename), "ZIP validation failed");
  }

  @Test
  void givenArchivedErrorFileWhenRetrieveErrorFileThenReturnDecryptedStream() throws IOException {
    Long organizationId = 1L;
    Path organizationPath = tempDir.resolve(String.valueOf(organizationId));
    Path sourcePath = Path.of("migration-data", "debt-positions-type-org-operators");
    String filename = "operators.csv";
    String errorFilename = "ERROR-operators.zip";
    Path errorDirectory = organizationPath.resolve(sourcePath).resolve("errors");
    Files.createDirectories(errorDirectory);
    Files.createFile(errorDirectory.resolve(errorFilename + AESUtils.CIPHER_EXTENSION));
    InputStream expectedStream = new java.io.ByteArrayInputStream("zip-content".getBytes());

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationPath);
    when(fileStorerServiceMock.decryptFile(errorDirectory, errorFilename)).thenReturn(expectedStream);

    InputStream result = service.retrieveErrorFile(organizationId, sourcePath, filename);

    assertSame(expectedStream, result);
  }

  /**
   * Helper method to add entries to the ZIP file
   */
  private static void addZipEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
    zos.putNextEntry(new ZipEntry(entryName));
    zos.write(content.getBytes());
    zos.closeEntry();
  }
}
