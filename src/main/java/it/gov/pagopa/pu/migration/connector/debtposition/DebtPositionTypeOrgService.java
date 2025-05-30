package it.gov.pagopa.pu.migration.connector.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionTypeOrg;

import java.util.Optional;

/**
 * This interface provides methods that manage debt positions type org within the related microservice.
 */
public interface DebtPositionTypeOrgService {

    /**
     * Get the debt position type org by code.
     *
     * @param code the code of the debt position type org.
     * @param orgId the organization id.
     * @return the {@link DebtPositionTypeOrg} object.
     */
    Optional<DebtPositionTypeOrg> getDebtPositionTypeOrgByCodeAndOrgId(String code, Long orgId, String accessToken);
}
