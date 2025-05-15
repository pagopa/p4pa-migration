package it.gov.pagopa.pu.migration.service.file;

import it.gov.pagopa.pu.migration.exception.InvalidFileException;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileArchiverServiceTest {
	private static final String TEST_PASSWORD = "mockPassword";

	@Mock
	private ZipFileService zipFileServiceMock;

	private FileArchiverService service;

	private final Path targetDir = Path.of("build", "tmp");

	@BeforeEach
	void setUp() {
		service = new FileArchiverService(TEST_PASSWORD, zipFileServiceMock);
	}

	@AfterEach
	void verifyNoMoreInteractions(){
		Mockito.verifyNoMoreInteractions(
				zipFileServiceMock
		);
	}

//region test compressAndArchive
	@Test
	void givenSuccessfulConditionsWhenCompressAndArchiveThenOk(@TempDir Path sourceDir) throws Exception {
		//given
		Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
		Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));
		List<Path> files = List.of(file1, file2);

		Path zipFilePath = Files.createFile(sourceDir.resolve("output.zip"));
		File mockZippedFile = zipFilePath.toFile();
		Path mockEncryptedFile = Files.copy(zipFilePath, sourceDir.resolve(zipFilePath.getFileName() + AESUtils.CIPHER_EXTENSION));

		when(zipFileServiceMock.zipper(zipFilePath, files)).thenReturn(mockZippedFile);
			assertTrue(Files.exists(mockEncryptedFile));

		try (MockedStatic<AESUtils> mockedAESUtils = mockStatic(AESUtils.class)) {
			mockedAESUtils.when(() -> AESUtils.encrypt(TEST_PASSWORD, mockZippedFile)).thenReturn(mockEncryptedFile.toFile());
			// when
			service.compressAndArchive(files, zipFilePath, targetDir);

			//then
			assertFalse(zipFilePath.toFile().exists(), "zipped file should be deleted");
			assertFalse(mockEncryptedFile.toFile().exists(), "encrypted file should be deleted from source directory");
			assertTrue(targetDir.resolve("output.zip" + AESUtils.CIPHER_EXTENSION).toFile().exists(), "Success");
		}
	}

	@Test
	void givenExceptionOnEncryptionWhenCompressAndArchiveThenThrowsIllegalStateException(@TempDir Path sourceDir) throws Exception {
		//given
		Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
		Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));
		List<Path> mockFiles = List.of(file1, file2);

		Path zipFilePath = Files.createFile(sourceDir.resolve("output.zip"));
		File mockZippedFile = zipFilePath.toFile();

		when(zipFileServiceMock.zipper(zipFilePath, mockFiles)).thenReturn(mockZippedFile);

		try (MockedStatic<AESUtils> mockedAESUtils = mockStatic(AESUtils.class)) {
			mockedAESUtils.when(() -> AESUtils.encrypt(TEST_PASSWORD, mockZippedFile)).thenThrow(IllegalStateException.class);

			// when then
			assertThrows(IllegalStateException.class,
				() -> service.compressAndArchive(mockFiles, zipFilePath, targetDir),
				"encryption failed");
		}
	}

	@Test
	void givenExceptionOnZippingWhenCompressAndArchiveThenThrowsInvalidFileException(@TempDir Path sourceDir) throws Exception {
		//given
		Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
		Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));
		List<Path> mockFiles = List.of(file1, file2);

		Path zipFilePath = Files.createFile(sourceDir.resolve("output.zip"));

		when(zipFileServiceMock.zipper(zipFilePath, mockFiles)).thenThrow(InvalidFileException.class);

		// when then
		assertThrows(InvalidFileException.class,
			() -> service.compressAndArchive(mockFiles, zipFilePath, targetDir),
			"zipping failed");
	}
//endregion
}
