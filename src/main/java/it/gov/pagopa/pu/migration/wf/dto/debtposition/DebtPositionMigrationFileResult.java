package it.gov.pagopa.pu.migration.wf.dto.debtposition;

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
}
