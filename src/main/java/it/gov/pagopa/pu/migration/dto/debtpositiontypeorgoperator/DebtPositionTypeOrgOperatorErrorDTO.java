package it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.dto.ErrorFileDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class DebtPositionTypeOrgOperatorErrorDTO extends ErrorFileDTO {

  private String ipaCode;
  private String debtPositionTypeOrgCode;
  private Long rowNumber;

  public DebtPositionTypeOrgOperatorErrorDTO(String fileName, String ipaCode, String debtPositionTypeOrgCode, Long rowNumber, String errorCode, String errorMessage) {
    super(fileName, errorCode, errorMessage);
    this.ipaCode = ipaCode;
    this.debtPositionTypeOrgCode = debtPositionTypeOrgCode;
    this.rowNumber = rowNumber;
  }

  @Override
  public String[] toCsvRow() {
    return new String[]{
      getFileName(), ipaCode, rowNumber.toString(),
      getErrorCode(), getErrorMessage()
    };
  }
}
