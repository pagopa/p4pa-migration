package it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class DebtPositionTypeOrgOperatorMigrationFileResult extends MigrationFileResult {
  private Long organizationId;
  private Long numTotalRows;
  private Long numCorrectlyProcessedRows;
}
