package it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class DebtPositionTypeOrgOperatorMigrationFileResult extends MigrationFileResult {
  private Long organizationId;
  private int numTotalRows;
  private int numCorrectlyProcessedRows;
  private List<DebtPositionTypeOrgOperatorErrorDTO> errorList;
}
