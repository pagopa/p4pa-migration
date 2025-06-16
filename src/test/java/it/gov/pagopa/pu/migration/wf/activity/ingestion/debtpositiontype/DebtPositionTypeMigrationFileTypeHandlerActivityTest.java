package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontype;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.security.SecurityUtils;
import it.gov.pagopa.pu.migration.service.AuthorizationService;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeMigrationFileTypeHandlerActivityTest {

    @Mock
    private UploadsRepository uploadsRepositoryMock;
    @Mock
    private MigrationFileRetrieverService fileRetrieverServiceMock;
    @Mock
    private FileArchiverService fileArchiverServiceMock;
    @Mock
    private FileShareService fileShareServiceMock;
    @Mock
    private AuthnService authnServiceMock;
    @Mock
    private OrganizationSearchClient organizationSearchClientMock;
    @Mock
    private ZipFileService zipFileServiceMock;

    private final PodamFactory podamFactory = new PodamFactoryImpl();

    private DebtPositionTypeMigrationFileTypeHandlerActivityImpl activity;

    @BeforeEach
    void setUp() {
        activity = new DebtPositionTypeMigrationFileTypeHandlerActivityImpl(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock,
          organizationSearchClientMock,
          zipFileServiceMock
        );
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock,
          organizationSearchClientMock
        );
    }


    @Test
    void testHandleRetrievedFiles_success(@TempDir Path sourceDir) throws Exception {
        Path file1 = Files.createFile(sourceDir.resolve("IPA12345-file1.txt"));

        Path zipFilePath = Files.createFile(sourceDir.resolve("output.zip"));
        File mockZippedFile = zipFilePath.toFile();
        Path mockEncryptedFile = Files.copy(zipFilePath, sourceDir.resolve(zipFilePath.getFileName() + AESUtils.CIPHER_EXTENSION));
        UserInfo loggedUser = podamFactory.manufacturePojo(UserInfo.class);

        when(uploadsRepositoryMock.findById(1L)).thenReturn(Optional.of(
          Uploads.builder()
            .organizationId(1L)
            .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE)
            .fileName("IPA12345-file1.txt")
            .filePathName(sourceDir.toString())
            .fileSize(123L)
            .updateOperatorExternalId("user")
            .build()
        ));

        when(authnServiceMock.getAccessToken("IPA12345")).thenReturn("tokenOrg");

        mockStatic(SecurityUtils.class).when(SecurityUtils::getLoggedUser).thenReturn(loggedUser);
        mockStatic(AuthorizationService.class).when(() -> AuthorizationService.getOrganizationIdFromUserInfo(loggedUser, "IPA99999")).thenReturn(1L);

        when(fileShareServiceMock.uploadIngestionFlowFile(
                anyLong(),
                any(),
                any(FileSystemResource.class),
                anyString()
        )).thenReturn(1L);

        String fileName = file1.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        Path zipFilePathForMock = file1.getParent().resolve(baseName + ".zip");

        when(zipFileServiceMock.zipper(
              zipFilePathForMock,
              List.of(file1)
        )).thenReturn(mockZippedFile);

        assertTrue(Files.exists(mockEncryptedFile));
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any()))
          .thenReturn(List.of(file1));

        MigrationFileResult result = activity.processFile(1L);
        assertNotNull(result);
        assertNotNull(result.getIngestionFlowFiles());
        assertNotNull(result.getIngestionFlowFiles().getFirst().getOrganizationId());
        verify(fileArchiverServiceMock).archive(any(Uploads.class));
    }

    @Test
    void testHandleRetrievedFiles_noFilesExtracted() {
        when(uploadsRepositoryMock.findById(3L)).thenReturn(Optional.of(
            Uploads.builder()
                .organizationId(1L)
                .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE)
                .fileName("IPA12345-file1.txt")
                .filePathName("/tmp")
                .fileSize(123L)
                .updateOperatorExternalId("user")
                .build()
        ));
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any())).thenReturn(List.of());
        Exception ex = assertThrows(
            it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException.class,
            () -> activity.processFile(3L)
        );
        assertTrue(ex.getMessage().contains("No file found in the uploaded archive"));
    }

    @Test
    void testHandleRetrievedFiles_invalidFileNameFormat(@TempDir Path sourceDir) throws Exception {
        Path file1 = Files.createFile(sourceDir.resolve("file1.txt")); // manca IPA code
        when(uploadsRepositoryMock.findById(4L)).thenReturn(Optional.of(
            Uploads.builder()
                .organizationId(1L)
                .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE)
                .fileName("file1.txt")
                .filePathName(sourceDir.toString())
                .fileSize(123L)
                .updateOperatorExternalId("user")
                .build()
        ));
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any())).thenReturn(List.of(file1));
        Exception ex = assertThrows(
            it.gov.pagopa.pu.migration.wf.exception.InvalidMigrationFileException.class,
            () -> activity.processFile(4L)
        );
        assertTrue(ex.getMessage().contains("Invalid file name format"));
    }

    @Test
    void testHandleRetrievedFiles_organizationNotManaged(@TempDir Path sourceDir) throws Exception {
        Path file1 = Files.createFile(sourceDir.resolve("IPA99999-file1.txt"));
        when(uploadsRepositoryMock.findById(5L)).thenReturn(Optional.of(
            Uploads.builder()
                .organizationId(1L)
                .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE)
                .fileName("IPA99999-file1.txt")
                .filePathName(sourceDir.toString())
                .fileSize(123L)
                .updateOperatorExternalId("user")
                .build()
        ));
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any())).thenReturn(List.of(file1));
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class);
             MockedStatic<AuthorizationService> authorizationServiceMockedStatic = mockStatic(AuthorizationService.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoggedUser).thenReturn(null);
            authorizationServiceMockedStatic.when(() -> AuthorizationService.getOrganizationIdFromUserInfo(null, "IPA99999")).thenReturn(null);
            Exception ex = assertThrows(
                InvalidMigrationFileException.class,
                () -> activity.processFile(5L)
            );
            assertTrue(ex.getMessage().contains("is not associated to managed organizations"));
        }
    }
}
