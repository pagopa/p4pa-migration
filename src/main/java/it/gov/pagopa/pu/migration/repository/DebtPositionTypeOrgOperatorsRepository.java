package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DebtPositionTypeOrgOperatorsRepository extends JpaRepository<DebtPositionTypeOrgOperators, Long> {

    /**
     * Finds an operator by its IPA code and debt position type organization code.
     *
     * @param organizationId the organization id
     * @param debtPositionTypeOrgCode the code of the debt position type organization
     * @return an Optional containing the operator if found, or empty if not found
     */
    Optional<DebtPositionTypeOrgOperators> findByOrganizationIdAndDebtPositionTypeOrgCode(Long organizationId, String debtPositionTypeOrgCode);

}
