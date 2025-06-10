package it.gov.pagopa.pu.migration.service.migration;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.dto.ErrorFileDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.ErrorArchiverService;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

@Slf4j
public abstract class MigrationProcessingService<T, R, E extends ErrorFileDTO> {

  private final ErrorArchiverService<E> errorArchiverService;

  protected MigrationProcessingService(ErrorArchiverService<E> errorArchiverService) {
    this.errorArchiverService = errorArchiverService;
  }

  /**
     * Generic processing loop for migration file rows.
     * Handles parsing errors, row counting, error archiving and updates the result.
     */
    protected void process(
            Iterator<T> iterator,
            List<CsvException> readerException,
            R migrationFileResult,
            Uploads uploads,
            List<E> errorList,
            ErrorDtoBuilder<E> errorDtoBuilder,
            Path workingDirectory
    ) {
      long processedRows = 0;
      long totalRows = 0;
      int[] previousReaderExceptionSize = {0};

      while (iterator.hasNext()) {
        totalRows = processReaderExceptions(readerException, uploads, previousReaderExceptionSize, errorList, totalRows, errorDtoBuilder);

        totalRows++;

        try {
          if (consumeRow(totalRows, iterator.next(), migrationFileResult, errorList, uploads)) {
            processedRows++;
          }
        } catch (Exception e){
          log.error("Not handled exception during Upload file processing: uploadId {}, lineNumber {}",
            uploads.getUploadId(),
            totalRows
            , e);
          errorList.add(errorDtoBuilder.buildErrorDto(uploads.getFileName(), totalRows, "PROCESSING_ERROR", e.getMessage()));
        }
      }
      totalRows = processReaderExceptions(readerException, uploads, previousReaderExceptionSize, errorList, totalRows, errorDtoBuilder);

      String errorsZipFileName = archiveErrorFiles(uploads, workingDirectory, errorList);

      setNumTotalRows(migrationFileResult, totalRows);
      setNumCorrectlyProcessedRows(migrationFileResult, processedRows);
      setErrorDescription(migrationFileResult,errorsZipFileName != null ? "Some rows have failed" : null);
      setDiscardedFileName(migrationFileResult,errorsZipFileName);


  }


    protected abstract boolean consumeRow(long lineNumber, T dto, R migrationFileResult, List<E> errorList, Uploads uploads);

    protected abstract void setNumTotalRows(R migrationFileResult, long totalRows);

    protected abstract void setNumCorrectlyProcessedRows(R migrationFileResult, long processedRows);

    protected abstract void setErrorDescription(R migrationFileResult, String errorDescription);

    protected abstract void setDiscardedFileName(R migrationFileResult, String discardedFileName);

    protected abstract void setErrorList(R migrationFileResult, List<E> errorList);

    protected abstract ErrorArchiverService<E> getErrorArchiverService();

    public interface ErrorDtoBuilder<E> {
        E buildErrorDto(String fileName, long lineNumber, String errorCode, String errorDescription);
    }

  private long processReaderExceptions(List<CsvException> readerExceptions, Uploads uploads, int[] previousReaderExceptionSize, List<E> errorList, long totalRows, ErrorDtoBuilder<E> errorDtoBuilder) {
    int readerExceptionDiff = readerExceptions.size() - previousReaderExceptionSize[0];
    if (readerExceptionDiff > 0) {
      readerExceptions.stream()
        .skip(previousReaderExceptionSize[0])
        .forEach(e ->
          errorList.add(
            errorDtoBuilder.buildErrorDto(
            uploads.getFileName(),
            e.getLineNumber(), "READER_EXCEPTION", e.getMessage()
          ))

        );

      previousReaderExceptionSize[0] = readerExceptions.size();
      totalRows += readerExceptionDiff;
    }
    return totalRows;
  }

    private String archiveErrorFiles(Uploads uploads, Path workingDirectory, List<E> errorList) {
      if (errorList.isEmpty()) {
        log.info("No errors to archive for file: {}", uploads.getFileName());
        return null;
      }

      errorArchiverService.writeErrors(workingDirectory, uploads, errorList);
      String errorsZipFileName = errorArchiverService.archiveErrorFiles(workingDirectory, uploads);
      log.info("Error file archived at: {}", errorsZipFileName);

      return errorsZipFileName;
    }
}


