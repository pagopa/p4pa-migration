package it.gov.pagopa.pu.migration.service.migration;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.service.file.ErrorArchiverService;

import java.util.Iterator;
import java.util.List;

public abstract class MigrationProcessingService<T, R, E extends it.gov.pagopa.pu.migration.dto.ErrorFileDTO> {

    /**
     * Generic processing loop for migration file rows.
     * Handles parsing errors, row counting, error archiving and updates the result.
     */
    protected void process(
            Iterator<T> iterator,
            List<CsvException> readerException,
            R migrationFileResult,
            Object context,
            List<E> errorList,
            ErrorDtoBuilder<E> errorDtoBuilder
    ) {
        // Handle CSV parsing errors
        if (readerException != null && !readerException.isEmpty()) {
            for (CsvException ex : readerException) {
                errorList.add(errorDtoBuilder.buildErrorDto(getFileNameFromContext(context), ex.getLineNumber(), "CSV_PARSE_ERROR", ex.getMessage()));
            }
            setErrorList(migrationFileResult, errorList);
            setNumTotalRows(migrationFileResult, 0);
            setNumCorrectlyProcessedRows(migrationFileResult, 0);
            return;
        }
        long lineNumber = 1;
        int processedRows = 0;
        int totalRows = 0;
        while (iterator.hasNext()) {
            T dto = iterator.next();
            totalRows++;
            boolean success = consumeRow(lineNumber, dto, migrationFileResult, errorList, context);
            if (success) {
                processedRows++;
            }
            lineNumber++;
        }
        setNumTotalRows(migrationFileResult, totalRows);
        setNumCorrectlyProcessedRows(migrationFileResult, processedRows);
        setErrorList(migrationFileResult, errorList);
    }

    protected abstract String getFileNameFromContext(Object context);

    protected abstract boolean consumeRow(long lineNumber, T dto, R migrationFileResult, List<E> errorList, Object context);

    protected abstract void setNumTotalRows(R migrationFileResult, int totalRows);

    protected abstract void setNumCorrectlyProcessedRows(R migrationFileResult, int processedRows);

    protected abstract void setErrorList(R migrationFileResult, List<E> errorList);

    protected abstract ErrorArchiverService<E> getErrorArchiverService();

    public interface ErrorDtoBuilder<E> {
        E buildErrorDto(String fileName, long lineNumber, String errorCode, String errorDescription);
    }
}
