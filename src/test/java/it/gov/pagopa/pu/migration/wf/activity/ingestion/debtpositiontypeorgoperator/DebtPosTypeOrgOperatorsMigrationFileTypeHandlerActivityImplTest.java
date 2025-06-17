package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.connector.organization.OrganizationService;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorProcessingService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorsErrorsArchiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImplTest {

    @Mock
    private DebtPosTypeOrgOperatorProcessingService processingServiceMock;
    @Mock
    private UploadsRepository uploadsRepositoryMock;
    @Mock
    private   MigrationFileRetrieverService fileRetrieverServiceMock;
    @Mock
    private FileArchiverService fileArchiverServiceMock;
    @Mock
    private FileShareService fileShareServiceMock;
    @Mock
    private AuthnService authnServiceMock;
    @Mock
    private OrganizationService organizationServiceMock;
    @Mock
    private ZipFileService zipFileServiceMock;
    @Mock
    private DebtPosTypeOrgOperatorsErrorsArchiverService errorsArchiverServiceMock;


    private DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl activity;

    @BeforeEach
    void setUp() {
        activity = new DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl(
          uploadsRepositoryMock,
          fileRetrieverServiceMock,
          fileArchiverServiceMock,
          fileShareServiceMock,
          authnServiceMock,
          organizationServiceMock,
          zipFileServiceMock,
          processingServiceMock,
          errorsArchiverServiceMock
        );
    }

    @Test
    void getHandledMigrationFileTypeReturnsCorrectEnum() {
        assertEquals(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS, activity.getHandledMigrationFileType());
    }

    @Test
    void handleRetrievedFilesProcessesAllFilesAndCountsCorrectly() {
        Path file1 = Path.of("file1.csv");
        Path file2 = Path.of("file2.csv");
        Uploads upload = new Uploads();
        upload.setFileSize(123L);

        DebtPositionTypeOrgOperatorMigrationFileResult result1 = mock(DebtPositionTypeOrgOperatorMigrationFileResult.class);
        DebtPositionTypeOrgOperatorMigrationFileResult result2 = mock(DebtPositionTypeOrgOperatorMigrationFileResult.class);

        when(result1.getNumCorrectlyProcessedRows()).thenReturn(10L);
        when(result2.getNumCorrectlyProcessedRows()).thenReturn(0L);

        when(processingServiceMock.processOperatorDebtPosTypeOrgFile(file1, upload)).thenReturn(result1);
        when(processingServiceMock.processOperatorDebtPosTypeOrgFile(file2, upload)).thenReturn(result2);

        MigrationFileResult result = activity.handleRetrievedFiles(List.of(file1, file2), upload);

        assertEquals(2, result.getNumTotalFiles());
        assertEquals(1, result.getNumCorrectlyProcessedFiles());
        assertEquals(123L, result.getFileSize());
    }

}
