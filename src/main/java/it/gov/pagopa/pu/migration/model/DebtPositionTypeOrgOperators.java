package it.gov.pagopa.pu.migration.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeOrgOperators extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operators_debt_pos_type_org_generator")
    @SequenceGenerator(name = "operators_debt_pos_type_org_generator", sequenceName = "operator_debt_pos_type_org_id_seq", allocationSize = 1)
    private Long operatorDebtPosTypeOrgId;
    @NotNull
    private Long organizationId;
    @NotNull
    private byte[] cfOperatorHash;
    @NotNull
    private Long debtPositionTypeOrgId;
    @NotNull
    private String debtPositionTypeOrgCode;
}
