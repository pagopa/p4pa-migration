package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileResult;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator.DebtPosTypeOrgOperatorProcessingService;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImplTest {

    private DebtPosTypeOrgOperatorProcessingService processingService;
    private DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl activity;

    @BeforeEach
    void setUp() {
        processingService = mock(DebtPosTypeOrgOperatorProcessingService.class);
        activity = new DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityImpl(
                null, null, null, null, null, null, null, processingService
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

        when(processingService.processOperatorDebtPosTypeOrgFile(file1, upload)).thenReturn(result1);
        when(processingService.processOperatorDebtPosTypeOrgFile(file2, upload)).thenReturn(result2);

        MigrationFileResult result = activity.handleRetrievedFiles(List.of(file1, file2), upload);

        assertEquals(2, result.getNumTotalFiles());
        assertEquals(1, result.getNumCorrectlyProcessedFiles());
        assertEquals(123L, result.getFileSize());
    }

}
