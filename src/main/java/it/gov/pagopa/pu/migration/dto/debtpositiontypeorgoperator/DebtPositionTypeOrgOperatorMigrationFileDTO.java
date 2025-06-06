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

  @CsvBindByName(column = "org_ipa_code", required = true)
  private String orgIpaCode;

  @CsvBindByName(column = "cf_operator_hash", required = true)
  private byte[] cfOperatorHash;

  @CsvBindByName(column = "debt_position_type_org_code", required = true)
  private String debtPositionTypeOrgCode;
}

