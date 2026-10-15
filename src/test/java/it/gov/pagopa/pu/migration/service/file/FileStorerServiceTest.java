package it.gov.pagopa.pu.migration.service.file;

import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.exception.FileUploadException;
import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import it.gov.pagopa.pu.migration.exception.common.ConflictException;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorerServiceTest {

  private FileStorerService fileStorerService;

  @TempDir
  private Path tempDir;

  private static final String FILE_ENCRYPT_PASSWORD = "testPassword";
  private final String sharedFolder = "build/tmp";

  @BeforeEach
  void setUp() {
    FoldersPathsConfig foldersPathsConfig = new FoldersPathsConfig();
    foldersPathsConfig.setShared(sharedFolder);
    FoldersPathsConfig.ProcessTargetSubFolders processTargetSubFolders = new FoldersPathsConfig.ProcessTargetSubFolders();
    processTargetSubFolders.setErrors("errors");
    processTargetSubFolders.setArchive("archive");
    foldersPathsConfig.setProcessTargetSubFolders(processTargetSubFolders);
    fileStorerService = new FileStorerService(foldersPathsConfig, FILE_ENCRYPT_PASSWORD);
  }

  @Test
  void givenInvalidFileWhenSaveToSharedFolderThenFileUploadException() {
    Assertions.assertThrows(InvalidFileException.class, () ->
        fileStorerService.saveToSharedFolder(0L, null, "", ""));
  }

  @Test
  void givenInvalidFilenameWhenSaveToSharedFolderThenInvalidFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Assertions.assertThrows(InvalidFileException.class, () ->
      fileStorerService.saveToSharedFolder(0L, file, "", "../test.txt"));
  }

  @Test
  void givenErrorWhenSaveToSharedFolderThenFileUploadException() throws IOException {
    MockMultipartFile fileSpy = spy(new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    InputStream inputStreamMock = mock(InputStream.class);
    doReturn(inputStreamMock)
      .when(fileSpy)
      .getInputStream();

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {
      aesUtilsMockedStatic.when(() -> AESUtils.encryptAndSave(FILE_ENCRYPT_PASSWORD,
          inputStreamMock,
          Path.of(sharedFolder).resolve(organizationId+"").resolve(relativePath),
          fileName))
        .thenThrow(new RuntimeException());

      Assertions.assertThrows(FileUploadException.class, () ->
        fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativePath, fileName));
    }
  }

  @Test
  void givenAlreadyUploadedFileWhenSaveToSharedFolderThenThrowConflictException() throws IOException {
    MockMultipartFile fileSpy = spy(new MockMultipartFile(
      "file",
      "testConflictFile.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    Path organizationFolder = Path.of(sharedFolder).resolve(organizationId + "");
    Path folderFile = organizationFolder.resolve(relativePath);
    Files.createDirectories(folderFile);
    Path tmpFile = Files.createFile(folderFile.resolve(fileName + AESUtils.CIPHER_EXTENSION));

    try {
      Assertions.assertThrows(ConflictException.class, () ->
        fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativePath, fileName));
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  @Test
  void givenAlreadyUploadedAndArchivedFileWhenSaveToSharedFolderThenThrowConflictException() throws IOException {
    MockMultipartFile fileSpy = spy(new MockMultipartFile(
      "file",
      "testConflictArchivedFile.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    Path organizationFolder = Path.of(sharedFolder).resolve(organizationId + "");
    Path folderFile = organizationFolder.resolve(relativePath).resolve("archive");
    Files.createDirectories(folderFile);
    Path tmpFile = Files.createFile(folderFile.resolve(fileName + AESUtils.CIPHER_EXTENSION));

    try {
      Assertions.assertThrows(ConflictException.class, () ->
        fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativePath, fileName));
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  @Test
  void givenValidFileWhenSaveToSharedFolderThenOK() throws IOException {
    MockMultipartFile fileSpy = spy(new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativeFilePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    InputStream inputStreamMock = mock(InputStream.class);
    doReturn(inputStreamMock)
      .when(fileSpy)
      .getInputStream();

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {

      String result = fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativeFilePath, fileName).getRelativePath();

      Assertions.assertEquals(relativeFilePath, result);
      aesUtilsMockedStatic.verify(() -> AESUtils.encryptAndSave(FILE_ENCRYPT_PASSWORD,
        inputStreamMock,
        Path.of(sharedFolder).resolve(organizationId+"").resolve(relativeFilePath),
        fileName));
    }
  }

  @Test
  void givenInvalidPathWhenSaveToSharedFolderThenInvalidFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Assertions.assertThrows(InvalidFileException.class, () ->
      fileStorerService.saveToSharedFolder(0L, file, "relative/../../test", ""));
  }

  @Test
  void givenExistingFileWhenDecryptFileThenReturnInputStreamResource() throws IOException {
    InputStream cipherInputStream = mock(ByteArrayInputStream.class);
    Path filePath = Path.of("build");
    String fileName = "fileName";

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {
      //noinspection resource
      aesUtilsMockedStatic.when(() -> AESUtils.decrypt(Mockito.eq(FILE_ENCRYPT_PASSWORD), Mockito.eq(filePath), Mockito.eq(fileName)))
        .thenReturn(cipherInputStream);

      try (InputStream result = fileStorerService.decryptFile(filePath, fileName)) {

        Assertions.assertNotNull(result);
        Assertions.assertSame(cipherInputStream, result);
      }
    }
  }

  @Test
  void givenFileNotExistsWhenCheckIfAlreadyUploadedOrArchivedThenReturnFalse() {
    String fileName = "notExistsFile";
    Long organizationId = 1L;

    boolean result = fileStorerService.checkIfAlreadyUploadedOrArchived(
      organizationId,
      sharedFolder,
      fileName
    );

    Assertions.assertFalse(result);
  }

  @Test
  void givenFileExistsInMainFolderWhenCheckIfAlreadyUploadedOrArchivedThenReturnTrue()
    throws IOException {
    //Given
    String archivedSubFolder = "archive";
    String fileName = "existsFile";
    Long organizationId = 1L;
    String cipherFileName = fileName + AESUtils.CIPHER_EXTENSION;

    Path mainFolderPath = tempDir.resolve(archivedSubFolder);
    Files.createDirectories(mainFolderPath);
    Files.createFile(mainFolderPath.resolve(cipherFileName));

    boolean result = fileStorerService.checkIfAlreadyUploadedOrArchived(
      organizationId,
      String.valueOf(mainFolderPath),
      fileName
    );

    // Then
    Assertions.assertTrue(result);
  }

}




