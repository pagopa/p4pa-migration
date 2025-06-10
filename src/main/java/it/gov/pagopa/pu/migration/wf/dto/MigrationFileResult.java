package it.gov.pagopa.pu.migration.wf.dto;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrationFileResult {
  private long fileSize;
  private int numTotalFiles;
  private int numCorrectlyProcessedFiles;
  private String errorDescription;
  private String discardedFileName;
  private List<IngestionFlowFile> ingestionFlowFiles;
}
