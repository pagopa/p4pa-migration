package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionErrorDTO;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionMigrationFileResult;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;
import it.gov.pagopa.pu.migration.wf.service.ingestion.ErrorArchiverService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Lazy
@Slf4j
public class DebtPositionProcessingService extends MigrationProcessingService<InstallmentIngestionFlowFileDTO, DebtPositionMigrationFileResult, DebtPositionErrorDTO> {

  private final CsvService csvService;
  private final DebtPositionErrorsArchiverService debtPositionErrorsArchiverService;

  public DebtPositionProcessingService(ErrorArchiverService<DebtPositionErrorDTO> errorArchiverService,
                                       CsvService csvService, DebtPositionErrorsArchiverService debtPositionErrorsArchiverService) {
    super(errorArchiverService);
    this.csvService = csvService;
    this.debtPositionErrorsArchiverService = debtPositionErrorsArchiverService;
  }

  @Override
  protected ErrorArchiverService<DebtPositionErrorDTO> getErrorArchiverService() {
    return debtPositionErrorsArchiverService;
  }

  @Override
  protected boolean consumeRow(long lineNumber, InstallmentIngestionFlowFileDTO dto, DebtPositionMigrationFileResult migrationFileResult, List<DebtPositionErrorDTO> errorList, Uploads uploads) {
    return true;
  }

  @Override
  protected void setNumTotalRows(DebtPositionMigrationFileResult result, long numTotalRows) {
    result.setNumTotalRows(numTotalRows);
  }

  @Override
  protected void setNumCorrectlyProcessedRows(DebtPositionMigrationFileResult result, long numCorrectlyProcessedRows) {
    result.setNumCorrectlyProcessedRows(numCorrectlyProcessedRows);
  }

  @Override
  protected void setErrorDescription(DebtPositionMigrationFileResult result, String errorDescription) {
    result.setErrorDescription(errorDescription);
  }

  @Override
  protected void setDiscardedFileName(DebtPositionMigrationFileResult result, String discardedFileName) {
    result.setDiscardedFileName(discardedFileName);
  }


private DebtPositionMigrationFileResult processDebtPositionFile(Iterator<InstallmentIngestionFlowFileDTO> iterator,
                                                                 List<CsvException> readerException,
                                                                 Uploads uploads,
                                                                 List<DebtPositionErrorDTO> errorList,
                                                                 Path workingDirectory,
                                                                 String fileName) {
    DebtPositionMigrationFileResult migrationFileResult = DebtPositionMigrationFileResult.builder().build();
    process(iterator,
      readerException,
      migrationFileResult,
      uploads,
      errorList,
      this::buildErrorDto,
      workingDirectory,
      fileName);
    return migrationFileResult;
  }

  private List<InstallmentIngestionFlowFileDTO> parseCsvFile(Path file, List<DebtPositionErrorDTO> errorList, List<String> unsuccessfulParsedFiles) {
    List<InstallmentIngestionFlowFileDTO> dtos = new ArrayList<>();
    try {
      csvService.readCsv(
        file,
        InstallmentIngestionFlowFileDTO.class,
        (csvIterator, readerException) -> {
          handleReaderExceptions(file, readerException, errorList, unsuccessfulParsedFiles);
          while (csvIterator.hasNext()) {
            InstallmentIngestionFlowFileDTO dto = csvIterator.next();
            InstallmentIngestionFlowFileRequiredFieldsValidator.setDefaultValues(dto);
            dtos.add(dto);
          }
          return null;
        }
      );
    } catch (IOException e) {
      log.error("IO error reading file {}: {}", file.getFileName(), e.getMessage(), e);
    }
    return dtos;
  }

  private void handleReaderExceptions(Path file, List<CsvException> readerException, List<DebtPositionErrorDTO> errorList, List<String> unsuccessfulParsedFiles) {
    if (readerException != null && !readerException.isEmpty()) {
      for (Exception ex : readerException) {
        DebtPositionErrorDTO error = new DebtPositionErrorDTO();
        error.setFileName(file.getFileName().toString());
        error.setErrorMessage(ex.getMessage());
        errorList.add(error);
        unsuccessfulParsedFiles.add(file.getFileName() + ":" + ex.getMessage());
      }
      log.error("Error reading file {}: {}", file.getFileName(), readerException);
    }
  }

  private static String buildErrorDescription(List<String> unsuccessfulParsedFiles) {
    String errorDescription = null;
    if (!unsuccessfulParsedFiles.isEmpty()) {
      errorDescription = "There were some errors during Debt positions migration.";
      errorDescription += "\n" + String.join("\n", unsuccessfulParsedFiles);
    }
    return errorDescription;
  }

  protected DebtPositionErrorDTO buildErrorDto(String fileName, long lineNumber, String errorCode, String message) {
    return DebtPositionErrorDTO.builder()
      .fileName(fileName)
      .rowNumber(lineNumber)
      .errorCode(errorCode)
      .errorMessage(message)
      .build();
  }


    public DebtPositionMigrationFileResult processMultipleDebtPositionFiles(List<Path> retrievedFiles, Uploads upload, List<DebtPositionErrorDTO> errorList) {
        if (retrievedFiles == null || retrievedFiles.isEmpty()) {
            return DebtPositionMigrationFileResult.builder()
                .parsedFiles(new ArrayList<>())
                .numCorrectlyProcessedFiles(0)
                .numTotalFiles(0)
                .numTotalRows(0L)
                .numCorrectlyProcessedRows(0L)
                .errorDescription(null)
                .build();
        }
        List<Path> parsedFiles = new ArrayList<>();
        List<String> unsuccessfulParsedFiles = new ArrayList<>();
        long totalRows = 0;
        long totalProcessedRows = 0;
        int numTotalFiles = 0;
        int numCorrectlyProcessedFiles = 0;

        for (Path file : retrievedFiles) {
            List<CsvException> readerException = new ArrayList<>();
            List<InstallmentIngestionFlowFileDTO> dtos = parseCsvFile(file, errorList, unsuccessfulParsedFiles);
            Iterator<InstallmentIngestionFlowFileDTO> iterator = dtos.iterator();
            DebtPositionMigrationFileResult singleResult = processDebtPositionFile(
                iterator,
                readerException,
                upload,
                errorList,
                file.getParent(),
                file.getFileName().toString()
            );
            String originalFileName = file.getFileName().toString();
            String tempFileName = "parsed-" + originalFileName.replaceFirst("\\.[^.]+$", "") + ".csv";
            Path parent = file.getParent();
            Path tempFile = parent.resolve(tempFileName);
            if (Files.exists(tempFile)) {
                parsedFiles.add(tempFile);
                numCorrectlyProcessedFiles++;
            } else {
                unsuccessfulParsedFiles.add(file.getFileName() + ": conversion failed");
            }
            numTotalFiles++;
            totalRows += singleResult.getNumTotalRows() != null ? singleResult.getNumTotalRows() : 0;
            totalProcessedRows += singleResult.getNumCorrectlyProcessedRows() != null ? singleResult.getNumCorrectlyProcessedRows() : 0;
        }

        return DebtPositionMigrationFileResult.builder()
            .parsedFiles(parsedFiles)
            .numCorrectlyProcessedFiles(numCorrectlyProcessedFiles)
            .numTotalFiles(numTotalFiles)
            .numTotalRows(totalRows)
            .numCorrectlyProcessedRows(totalProcessedRows)
            .errorDescription(buildErrorDescription(unsuccessfulParsedFiles))
            .build();
    }
}
