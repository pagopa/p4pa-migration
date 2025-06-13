package it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.opencsv.bean.CsvBindByName;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DebtPositionTypeOrgOperatorMigrationFileDTO {

  @CsvBindByName(column = "enteIpaCode", required = true)
  private String orgIpaCode;

  @CsvBindByName(column = "cfOperatore", required = true)
  private String cfOperator;

  @CsvBindByName(column = "codiceTipoDovuto", required = true)
  private String debtPositionTypeOrgCode;
}

