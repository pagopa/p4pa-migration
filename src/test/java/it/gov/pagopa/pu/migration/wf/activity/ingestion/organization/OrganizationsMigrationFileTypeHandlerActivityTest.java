package it.gov.pagopa.pu.migration.wf.activity.ingestion.organization;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivityImpl;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationsMigrationFileTypeHandlerActivityTest {

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

    private OrganizationsMigrationFileTypeHandlerActivityImpl activity;

    @BeforeEach
    void setUp() {
        activity = new OrganizationsMigrationFileTypeHandlerActivityImpl(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock
        );
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock
        );
    }


    @Test
    void testHandleRetrievedFiles_success() {
        when(uploadsRepositoryMock.findById(1L)).thenReturn(Optional.of(
          Uploads.builder()
            .organizationId(1L)
            .fileType(MigrationFileTypeEnum.ORGANIZATIONS)
            .fileName("test.csv")
            .filePathName("/tmp")
            .fileSize(123L)
            .updateOperatorExternalId("user")
            .build()
        ));
        when(authnServiceMock.getAccessToken()).thenReturn("token");
        when(fileShareServiceMock.uploadIngestionFlowFile(
                anyLong(),
                any(),
                any(FileSystemResource.class),
                anyString()
        )).thenReturn(1L);
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(anyLong(), any(), any()))
            .thenReturn(List.of(Path.of("/tmp/test.csv")));

        MigrationFileResult result = activity.processFile(1L);
        assertNotNull(result);
        assertNotNull(result.getIngestionFlowFiles());
        assertNotNull(result.getIngestionFlowFiles().get(0).getOrganizationId());
        verify(fileShareServiceMock, times(1)).uploadIngestionFlowFile(
                eq(1L),
                eq(IngestionFlowFileType.ORGANIZATIONS),
                any(FileSystemResource.class),
                eq("token")
        );
        verify(fileArchiverServiceMock).archive(any(Uploads.class));
    }

    @Test
    void testHandleRetrievedFiles_organizationIdNull_shouldThrow() {
        when(uploadsRepositoryMock.findById(1L)).thenReturn(Optional.of(
          Uploads.builder()
            .organizationId(null)
            .fileType(MigrationFileTypeEnum.ORGANIZATIONS)
            .fileName("test.csv")
            .filePathName("/tmp")
            .fileSize(123L)
            .build()
        ));
        when(fileRetrieverServiceMock.retrieveAndUnzipFile(any(), any(), any()))
            .thenReturn(List.of(Path.of("/tmp/test.csv")));

        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(
            it.gov.pagopa.pu.migration.wf.exception.InvalidIngestionFileException.class,
            () -> activity.processFile(1L)
        );
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("organizationId is required"));
        verify(fileRetrieverServiceMock).retrieveAndUnzipFile(any(), any(), any());
        verify(uploadsRepositoryMock, times(1)).findById(1L);
    }

}
