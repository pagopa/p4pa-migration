package it.gov.pagopa.pu.migration.wf.dto.debtposition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.StatefulBeanToCsv;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class DebtPositionMigrationFileResult extends MigrationFileResult {
  private List<Path> parsedFiles;
  private Long numTotalRows;
  private Long numCorrectlyProcessedRows;

  @JsonIgnore
  private StatefulBeanToCsv<InstallmentIngestionFlowFileDTO> lastCsvWriter;
}
