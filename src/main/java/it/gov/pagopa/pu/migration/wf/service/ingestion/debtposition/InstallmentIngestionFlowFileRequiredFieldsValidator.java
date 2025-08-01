package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;


import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InstallmentIngestionFlowFileRequiredFieldsValidator {

    private InstallmentIngestionFlowFileRequiredFieldsValidator() {
    }

    public static final String CREATION_DATE_FORMAT = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    public static void setDefaultValues(InstallmentIngestionFlowFileDTO dto){
        dto.setFlagPuPagoPaPayment(Boolean.FALSE);

        if (dto.getFlagMultiBeneficiary() == null) {
            dto.setFlagMultiBeneficiary(Boolean.FALSE);
        }

        if (dto.getNumberBeneficiary() == null) {
            dto.setNumberBeneficiary(Boolean.TRUE.equals(dto.getFlagMultiBeneficiary()) ? 1 : 0);
        }

        setDefaultIfNotLastVersion(dto);
    }

    private static void setDefaultIfNotLastVersion(InstallmentIngestionFlowFileDTO dto) {
        if (dto.getDescription() == null) {
            dto.setDescription(String.format("DebtPosition with code %s was created on %s", dto.getDebtPositionTypeCode(), CREATION_DATE_FORMAT));
        }
        if (dto.getPaymentOptionIndex() == null) {
            dto.setPaymentOptionIndex(1);
        }
        if (dto.getPaymentOptionType() == null) {
            dto.setPaymentOptionType("SINGLE_INSTALLMENT");
        }
        if (dto.getPaymentOptionDescription() == null) {
            dto.setPaymentOptionDescription("Pagamento Singolo Avviso");
        }
    }
}
