package it.gov.pagopa.pu.migration.wf.mapper;

import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.migration.utils.HashAlgorithm;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class DebtPositionTypeOrgOperatorMapper {
  private final HashAlgorithm hashAlgorithm;

  public DebtPositionTypeOrgOperatorMapper(@Value("${data-cipher.hash-pepper}") String hashPepper) {
    hashAlgorithm = new HashAlgorithm("SHA-256", Base64.getDecoder().decode(hashPepper));
  }

  public DebtPositionTypeOrgOperators mapToOperators(DebtPositionTypeOrgOperatorMigrationFileDTO dto,
                                                     Long debtPositionTypeOrgId,
                                                     Long organizationId) {
    if (dto == null) {
      return null;
    }
    byte [] cfOperatorHash = hashFiscalCode(dto.getCfOperator());
    return DebtPositionTypeOrgOperators.builder()
      .cfOperatorHash(cfOperatorHash)
      .organizationId(organizationId)
      .debtPositionTypeOrgCode(dto.getDebtPositionTypeOrgCode())
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .build();
  }

  @SuppressWarnings("squid:S1168") // null String if hashed should return still null
  public byte[] hashFiscalCode(String value) {
    if (value == null) {
      return null;
    }
    return hashAlgorithm.apply(value.toUpperCase());
  }
}
