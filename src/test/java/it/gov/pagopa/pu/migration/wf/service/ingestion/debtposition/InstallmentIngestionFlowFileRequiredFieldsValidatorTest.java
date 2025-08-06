package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static it.gov.pagopa.pu.debtposition.dto.generated.Action.I;
import static it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition.InstallmentIngestionFlowFileRequiredFieldsValidator.setDefaultValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstallmentIngestionFlowFileRequiredFieldsValidatorTest {

    @Test
    void givenObligatoryFieldsNullWhenValidateRequiredFieldsThenOk(){
        InstallmentIngestionFlowFileDTO dto = buildInstallmentIngestionFlowFileDTO();
        setDefaultValues(dto);
        assertEquals("false", dto.getFlagPuPagoPaPayment());
        assertEquals("false", dto.getFlagMultiBeneficiary());
        assertEquals("0", dto.getNumberBeneficiary());
        assertNotNull(dto.getDescription());
        assertEquals("1", dto.getPaymentOptionIndex());
        assertEquals("SINGLE_INSTALLMENT", dto.getPaymentOptionType());
        assertEquals("Pagamento Singolo Avviso", dto.getPaymentOptionDescription());
    }

    @Test
    void givenFlagMultiBeneficiaryTrueWhenValidateRequiredFieldsThenOk(){
        InstallmentIngestionFlowFileDTO dto = buildInstallmentIngestionFlowFileDTO();
        dto.setFlagMultiBeneficiary("true");
        setDefaultValues(dto);
        assertEquals("1", dto.getNumberBeneficiary());
    }

    @Test
    void givenObligatoryFieldsNotNullWhenValidateRequiredFieldsThenDoNothing(){
        InstallmentIngestionFlowFileDTO dto = buildInstallmentIngestionFlowFileDTO();
        dto.setFlagMultiBeneficiary("true");
        dto.setFlagPuPagoPaPayment("false");
        dto.setNumberBeneficiary("3");
        dto.setDescription("DP Description");
        dto.setPaymentOptionIndex("3");
        dto.setPaymentOptionType("Payment Option Type");
        dto.setPaymentOptionDescription("Payment option Description");
        setDefaultValues(dto);
        assertEquals("false", dto.getFlagPuPagoPaPayment());
        assertEquals("true", dto.getFlagMultiBeneficiary());
        assertEquals("3", dto.getNumberBeneficiary());
        assertEquals("DP Description", dto.getDescription());
        assertEquals("3", dto.getPaymentOptionIndex());
        assertEquals("Payment Option Type", dto.getPaymentOptionType());
        assertEquals("Payment option Description", dto.getPaymentOptionDescription());
    }

    private static InstallmentIngestionFlowFileDTO buildInstallmentIngestionFlowFileDTO() {
        InstallmentIngestionFlowFileDTO dto = new InstallmentIngestionFlowFileDTO();
        dto.setEntityType(PersonEntityType.F.getValue());
        dto.setFiscalCode("FiscalCode");
        dto.setFullName("FullName");
        dto.setAmount(BigDecimal.TEN.toString());
        dto.setDebtPositionTypeCode("DebtPositionTypeCode");
        dto.setRemittanceInformation("RemittanceInformation");
        dto.setAction(I.getValue());
        return dto;
    }
}
