package it.gov.pagopa.pu.migration.wf.dto;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;

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
  private List<Pair<Long, IngestionFlowFileType>> ingestionFlowFileIds;
}
