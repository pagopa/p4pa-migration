package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import com.opencsv.bean.StatefulBeanToCsv;
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

import java.io.Writer;
import java.nio.file.Files;
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
    String fileName = uploads.getFileName();
    InstallmentIngestionFlowFileRequiredFieldsValidator.setDefaultValues(dto);
    if (migrationFileResult.getLastCsvWriter() == null) {
      DebtPositionErrorDTO error = buildErrorDto(null, lineNumber, "CSV_WRITER_NULL", "CsvWriter not initialized");
      errorList.add(error);
      log.error("CsvWriter not initialized at row {}", lineNumber);
      return false;
    }
    StatefulBeanToCsv<InstallmentIngestionFlowFileDTO> csvWriter = migrationFileResult.getLastCsvWriter();
    try {
      csvWriter.write(dto);
      migrationFileResult.setNumCorrectlyProcessedRows(migrationFileResult.getNumCorrectlyProcessedRows() + 1);
    } catch (Exception e) {
      DebtPositionErrorDTO error = buildErrorDto(fileName, lineNumber, "CSV_WRITE_ERROR", e.getMessage());
      errorList.add(error);
      log.error("Error writing row {} in file {}: {}", lineNumber, fileName, e.getMessage(), e);
      return false;
    }
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
   * Processes a debt position CSV file by reading its contents, mapping each row to a DTO,
   * and writing the processed data to a new CSV file using a StatefulBeanToCsv writer.
   * <p>
   * This method performs the following steps:
   * <ul>
   *   <li>Reads the input CSV file and maps each row to an {@link InstallmentIngestionFlowFileDTO}.</li>
   *   <li>Creates a new CSV file for the processed output using the provided profile.</li>
   *   <li>Processes each DTO, writing valid rows to the output CSV and collecting errors.</li>
   *   <li>Returns a {@link DebtPositionMigrationFileResult} containing the processing outcome.</li>
   * </ul>
   *
   * @param file      the path to the input CSV file to process
   * @param uploads   the upload metadata associated with the file
   * @param errorList the list to which any processing errors will be added
   * @return a {@link DebtPositionMigrationFileResult} with the results of the processing
   */
  public DebtPositionMigrationFileResult processDebtPositionFile(Path file,
                                                                Uploads uploads,
                                                                List<DebtPositionErrorDTO> errorList) {
    List<InstallmentIngestionFlowFileDTO> dtos;
    try {
      dtos = csvService.readCsv(
        file,
        InstallmentIngestionFlowFileDTO.class,
        (iterator, exceptions) -> {
          List<InstallmentIngestionFlowFileDTO> list = new ArrayList<>();
          iterator.forEachRemaining(list::add);
          return list;
        }
      );
    } catch (Exception e) {
      log.error("Error reading file {}: {}", file, e.getMessage(), e);
      return DebtPositionMigrationFileResult.builder().build();
    }
    Path workingDirectory = file.getParent();
    String fileName = file.getFileName().toString();
    Path csvFilePath = workingDirectory.resolve("parsed-" + fileName.replaceFirst("\\.[^.]+$", "") + ".csv");
    List<CsvException> readerException = new ArrayList<>();
    try (Writer writer = Files.newBufferedWriter(csvFilePath)) {
      StatefulBeanToCsv<InstallmentIngestionFlowFileDTO> csvWriter = csvService.createCsvWriter(
        InstallmentIngestionFlowFileDTO.class,
        "V2_0",
        writer
      );
      DebtPositionMigrationFileResult migrationFileResult = DebtPositionMigrationFileResult.builder()
        .lastCsvWriter(csvWriter)
        .build();
      process(dtos.iterator(),
        readerException,
        migrationFileResult,
        uploads,
        errorList,
        this::buildErrorDto,
        workingDirectory,
        fileName);
      return migrationFileResult;
    } catch (Exception e) {
      log.error("Error creating CSV writer for file {}: {}", csvFilePath, e.getMessage(), e);
      return DebtPositionMigrationFileResult.builder().build();
    }
  }

  protected DebtPositionErrorDTO buildErrorDto(String fileName, long lineNumber, String errorCode, String message) {
    return DebtPositionErrorDTO.builder()
      .fileName(fileName)
      .rowNumber(lineNumber)
      .errorCode(errorCode)
      .errorMessage(message)
      .build();
  }

}
