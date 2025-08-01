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
import java.nio.file.Path;
import java.util.ArrayList;
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



  /**
   * Reads and parses the given CSV files, applies default values to each DTO, and collects errors during parsing.
   * Returns a DebtPositionMigrationFileResult containing the processed files and statistics.
   *
   * @param retrievedFiles the list of input CSV files to process
   * @param errorList the list to populate with DebtPositionErrorDTO errors
   * @return DebtPositionMigrationFileResult with processed files and stats
   */
  public DebtPositionMigrationFileResult readAndParseRows(List<Path> retrievedFiles, List<DebtPositionErrorDTO> errorList) {
    int numTotalFiles = 0;
    int numCorrectlyProcessedFiles = 0;
    List<Path> parsedFiles = new ArrayList<>();
    List<String> unsuccessfulParsedFiles = new ArrayList<>();

    for (Path file : retrievedFiles) {
      processSingleFile(file, errorList, parsedFiles, unsuccessfulParsedFiles);
      numTotalFiles++;
    }

    numCorrectlyProcessedFiles = parsedFiles.size();

    return DebtPositionMigrationFileResult.builder()
      .parsedFiles(parsedFiles)
      .numCorrectlyProcessedFiles(numCorrectlyProcessedFiles)
      .numTotalFiles(numTotalFiles)
      .errorDescription(buildErrorDescription(unsuccessfulParsedFiles))
      .build();
  }

  private void processSingleFile(Path file, List<DebtPositionErrorDTO> errorList, List<Path> parsedFiles, List<String> unsuccessfulParsedFiles) {
    try {
      List<InstallmentIngestionFlowFileDTO> dtos = parseCsvFile(file, errorList, unsuccessfulParsedFiles);
      // Usa la stessa logica di handleFilesUpload per il nome del file zip temporaneo
      String originalFileName = file.getFileName().toString();
      String tempFileName = originalFileName.replaceFirst("\\.[^.]+$", "") + "-parsed.csv";
      Path tempFile = file.getParent() != null ? file.getParent().resolve(tempFileName) : Path.of(System.getProperty("java.io.tmpdir")).resolve(tempFileName);
      csvService.createCsv(tempFile, InstallmentIngestionFlowFileDTO.class, () -> dtos, "V2_0");
      log.info("Processed {} rows from file {} into {}", dtos.size(), file.getFileName(), tempFile.getFileName());
      parsedFiles.add(tempFile);
    } catch (Exception e) {
      if (errorList == null) {
        errorList = new ArrayList<>();
      }
      DebtPositionErrorDTO error = new DebtPositionErrorDTO();
      error.setFileName(file.getFileName().toString());
      error.setErrorMessage(e.getMessage());
      errorList.add(error);
      unsuccessfulParsedFiles.add(file.getFileName() + ":" + e.getMessage());
      log.error("Error processing file {}: {}", file.getFileName(), e.getMessage(), e);
    }
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

}
