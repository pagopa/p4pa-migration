package it.gov.pagopa.pu.migration.service.migration;

import com.opencsv.exceptions.CsvException;
import it.gov.pagopa.pu.migration.dto.ErrorFileDTO;
import it.gov.pagopa.pu.migration.service.file.ErrorArchiverService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MigrationProcessingServiceTest {

  static class DummyError extends ErrorFileDTO {
    @Override
    public String[] toCsvRow() {
      return new String[0];
    }
  }

  static class DummyResult {
    int totalRows;
    int processedRows;
    List<DummyError> errorList;
  }

  static class DummyService extends MigrationProcessingService<String, DummyResult, DummyError> {
    @Override
    protected String getFileNameFromContext(Object context) {
      return "file.csv";
    }

    @Override
    protected boolean consumeRow(long lineNumber, String dto, DummyResult migrationFileResult, List<DummyError> errorList, Object context) {
      if (dto.equals("error")) {
        errorList.add(new DummyError());
        return false;
      }
      return true;
    }

    @Override
    protected void setNumTotalRows(DummyResult migrationFileResult, int totalRows) {
      migrationFileResult.totalRows = totalRows;
    }

    @Override
    protected void setNumCorrectlyProcessedRows(DummyResult migrationFileResult, int processedRows) {
      migrationFileResult.processedRows = processedRows;
    }

    @Override
    protected void setErrorList(DummyResult migrationFileResult, List<DummyError> errorList) {
      migrationFileResult.errorList = errorList;
    }

    @Override
    protected ErrorArchiverService<DummyError> getErrorArchiverService() {
      return null;
    }
  }

  @Test
  void processHandlesCsvParseErrors() {
    DummyService service = new DummyService();
    DummyResult result = new DummyResult();
    List<CsvException> csvExceptions = List.of(new CsvException("parse error"));
    List<DummyError> errorList = new ArrayList<>();
    service.process(List.<String>of("a", "b").iterator(), csvExceptions, result, null, errorList, (file, line, code, desc) -> new DummyError());
    Assertions.assertEquals(1, result.errorList.size());
    Assertions.assertEquals(0, result.totalRows);
    Assertions.assertEquals(0, result.processedRows);
  }

  @Test
  void processCountsRowsAndErrorsCorrectly() {
    DummyService service = new DummyService();
    DummyResult result = new DummyResult();
    List<DummyError> errorList = new ArrayList<>();
    service.process(List.of("ok", "error", "ok").iterator(), List.of(), result, null, errorList, (file, line, code, desc) -> new DummyError());
    Assertions.assertEquals(1, result.errorList.size());
    Assertions.assertEquals(3, result.totalRows);
    Assertions.assertEquals(2, result.processedRows);
  }


  @Test
  void processHandlesAllErrors() {
    DummyService service = new DummyService();
    DummyResult result = new DummyResult();
    List<DummyError> errorList = new ArrayList<>();
    service.process(List.of("error", "error").iterator(), List.of(), result, null, errorList, (file, line, code, desc) -> new DummyError());
    Assertions.assertEquals(2, result.errorList.size());
    Assertions.assertEquals(2, result.totalRows);
    Assertions.assertEquals(0, result.processedRows);
  }
}
