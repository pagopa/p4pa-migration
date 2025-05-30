package it.gov.pagopa.pu.migration.wf.activity.ingestion.organization;

import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivityImpl;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private OrganizationSearchClient organizationSearchClientMock;

    private OrganizationsMigrationFileTypeHandlerActivityImpl activity;

    @BeforeEach
    void setUp() {
        activity = new OrganizationsMigrationFileTypeHandlerActivityImpl(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock,
          organizationSearchClientMock
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

        when(organizationSearchClientMock.getByOrganizationId(1L, "token")).thenReturn(
          Organization.builder()
            .ipaCode("IPA12345")
            .orgFiscalCode("ORG12345")
            .orgName("Organization Name")
            .orgTypeCode("ORG_TYPE")
            .status(OrganizationStatus.ACTIVE)
            .flagNotifyOutcomePush(false)
            .flagPaymentNotification(false)
            .flagNotifyIo(false)
            .build()
        );
        when(authnServiceMock.getAccessToken("IPA12345")).thenReturn("tokenOrg");
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
        assertNotNull(result.getIngestionFlowFiles().getFirst().getOrganizationId());
        verify(fileArchiverServiceMock).archive(any(Uploads.class));
    }
}
