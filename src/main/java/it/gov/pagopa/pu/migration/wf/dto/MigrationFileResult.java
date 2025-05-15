package it.gov.pagopa.pu.migration.wf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrationFileResult {
  private long fileSize;
  private int numTotalFiles;
  private int numCorrectlyProcessedFiles;
  private String errorDescription;
}
