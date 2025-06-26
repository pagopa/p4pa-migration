package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DebtPositionTypeOrgOperatorsRepository extends JpaRepository<DebtPositionTypeOrgOperators, Long> {

    /**
     * Finds the first operator by organizationId, debt position type organization code, and operator's fiscal code hash.
     *
     * @param organizationId the organization id
     * @param debtPositionTypeOrgCode the code of the debt position type organization
     * @param cfOperatorHash the hash of the operator's fiscal code
     * @return an Optional containing the operator if found, or empty if not found
     */
    Optional<DebtPositionTypeOrgOperators> findFirstByOrganizationIdAndDebtPositionTypeOrgCodeAndCfOperatorHash(Long organizationId, String debtPositionTypeOrgCode, byte[] cfOperatorHash);

}
