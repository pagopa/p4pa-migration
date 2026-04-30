package it.gov.pagopa.pu.migration.service.file;

import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZipFileServiceTest {
  private static final int MAX_ENTRIES = 1000;
  private static final long MAX_UNCOMPRESSED_SIZE = 50 * 1024 * 1024L;
  private static final double MAX_COMPRESSION_RATIO = 0.1;

  @TempDir
  private Path tempDir;

  private Path zipFile;
  private ZipFileService service;

  @BeforeEach
  void setup() throws IOException {
    service = new ZipFileService(MAX_ENTRIES, MAX_UNCOMPRESSED_SIZE, MAX_COMPRESSION_RATIO);
    zipFile = tempDir.resolve("test.zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      addZipEntry(zos, "file1.txt", "This is the content of file1.");
      addZipEntry(zos, "file2.txt", "This is the content of file2.");
    }
  }

  @Test
  void testUnzipValidArchive() throws IOException {
    Path outputDir = tempDir.resolve("output");
    System.out.println(outputDir);
    assertDoesNotThrow(() -> service.unzip(zipFile, outputDir));
    assertTrue(Files.exists(outputDir.resolve("file1.txt")));
    assertTrue(Files.exists(outputDir.resolve("file2.txt")));

    String contentFile1 = Files.readString(outputDir.resolve("file1.txt"));
    String contentFile2 = Files.readString(outputDir.resolve("file2.txt"));
    assertEquals("This is the content of file1.", contentFile1);
    assertEquals("This is the content of file2.", contentFile2);
  }

  @Test
  void testUnzipArchiveWithDirectory() throws IOException {
    Path outputDir = tempDir.resolve("output");
    Path zipWithDirectoryFile = tempDir.resolve("test-with-dir.zip");


    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipWithDirectoryFile))) {
      addZipEntry(zos, "file1.txt", "This is the content of file1.");
      addZipEntry(zos, "file2.txt", "This is the content of file2.");

      zos.putNextEntry(new ZipEntry("subfolder/"));
      zos.closeEntry();

      addZipEntry(zos, "subfolder/nested-file.txt", "Nested content.");
    }

    assertDoesNotThrow(() -> service.unzip(zipWithDirectoryFile, outputDir));
  }

  @Test
  void testUnzipWithExcessiveEntries() throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      for (int i = 0; i < 1100; i++) {
        addZipEntry(zos, "file" + i + ".txt", "Content of file " + i);
      }
    }

    Path outputDir = tempDir.resolve("output");
    assertThrows(InvalidFileException.class,
      () -> service.unzip(zipFile, outputDir), "exceeds the maximum number of entries"
    );
  }

  @Test
  void testUnzipWithMockedFileSystem() {
    Path mockZip = mock(Path.class);
    when(mockZip.toFile()).thenReturn(zipFile.toFile());

    Path outputDir = tempDir.resolve("output");

    assertDoesNotThrow(() -> service.unzip(mockZip, outputDir));
    verify(mockZip, atLeastOnce()).toFile();
  }

  @Test
  void testUnzipWithZipSlipAttack() throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      zos.putNextEntry(new ZipEntry("../malicious.txt"));
      zos.write("Malicious content".getBytes());
      zos.closeEntry();
    }

    Path outputDir = tempDir.resolve("output");
    assertThrows(InvalidFileException.class,
      () -> service.unzip(zipFile, outputDir), "Bad zip entry"
    );
  }

  @Test
  void testUnzipWithZeroCompressedSize() throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      ZipEntry zeroSizeEntry = new ZipEntry("empty.txt");
      zos.putNextEntry(zeroSizeEntry);
      zos.closeEntry();
    }

    Path outputDir = tempDir.resolve("output");
    assertDoesNotThrow(() -> service.unzip(zipFile, outputDir));

    assertTrue(Files.exists(outputDir.resolve("empty.txt")), "Expected empty.txt to exist");
  }

  @Test
  void testUnzipWithExcessiveUncompressedSize() throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      byte[] largeContent = new byte[(int) (MAX_UNCOMPRESSED_SIZE / 2)];
      for (int i = 0; i < 3; i++) {
        addZipEntry(zos, "largeFile" + i + ".txt", new String(largeContent));
      }
    }

    Path outputDir = tempDir.resolve("output");
    assertThrows(InvalidFileException.class,
      () -> service.unzip(zipFile, outputDir), "ZIP file exceeds the maximum allowed uncompressed size");
  }

  @Test
  void testZipperValidFiles() throws IOException {
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");

    Files.writeString(file1, "Content of file1");
    Files.writeString(file2, "Content of file2");

    Path zipPath = tempDir.resolve("output.zip");
    File zipped = service.zipper(zipPath, List.of(file1, file2));

    assertTrue(zipped.exists());
    assertTrue(zipped.isFile());
  }

  @Test
  void testZipperWithInvalidFileName() {
    Path noSuchPlace = Path.of("/no/such/place");
    List<Path> list = List.of(noSuchPlace);
    Path zipPath = tempDir.resolve("output.zip");
    assertThrows(InvalidFileException.class,
      () -> service.zipper(zipPath, list),
      "Error compressing non-existent file");
  }

  /**
   * Helper method to add entries to the ZIP file
   */
  private static void addZipEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
    zos.putNextEntry(new ZipEntry(entryName));
    zos.write(content.getBytes());
    zos.closeEntry();
  }

  @Test
  void givenMacosZipWhenUnzipThenOk() {
    // Given
    Path macosGeneratedZipFile = Path.of("src").resolve("test").resolve("resources").resolve("macosGeneratedZip.zip");

    // When
    List<Path> unzip = service.unzip(macosGeneratedZipFile, tempDir);

    // Then
    Assertions.assertEquals(1, unzip.size());
    Assertions.assertEquals(tempDir.resolve("prova.csv"), unzip.getFirst());
  }
}
