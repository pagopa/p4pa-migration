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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_org_operator_generator")
    @SequenceGenerator(name = "debt_position_type_org_operator_generator", sequenceName = "debt_position_type_org_operator_id_seq", allocationSize = 1)
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
