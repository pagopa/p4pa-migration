package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;


import it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InstallmentIngestionFlowFileRequiredFieldsValidator {

    private InstallmentIngestionFlowFileRequiredFieldsValidator() {
    }

    public static final String CREATION_DATE_FORMAT = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    public static void setDefaultValues(InstallmentIngestionFlowFileDTO dto){
        dto.setFlagPuPagoPaPayment(Boolean.FALSE.toString());

        if (dto.getFlagMultiBeneficiary() == null) {
            dto.setFlagMultiBeneficiary(Boolean.FALSE.toString());
        }

        if (dto.getNumberBeneficiary() == null) {
            dto.setNumberBeneficiary("true".equalsIgnoreCase(dto.getFlagMultiBeneficiary()) ? "2" : "1");
        }

        setDefaultIfNotLastVersion(dto);
    }

    private static void setDefaultIfNotLastVersion(InstallmentIngestionFlowFileDTO dto) {
        if (dto.getDescription() == null) {
            dto.setDescription(String.format("DebtPosition with code %s was created on %s", dto.getDebtPositionTypeCode(), CREATION_DATE_FORMAT));
        }
        if (dto.getPaymentOptionIndex() == null) {
            dto.setPaymentOptionIndex("1");
        }
        if (dto.getPaymentOptionType() == null) {
            dto.setPaymentOptionType("SINGLE_INSTALLMENT");
        }
        if (dto.getPaymentOptionDescription() == null) {
            dto.setPaymentOptionDescription("Pagamento Singolo Avviso");
        }

      if (dto.getOrgFiscalCode2() == null && dto.getOrgFiscalCodeSecondario() != null) {
        dto.setOrgFiscalCode2(dto.getOrgFiscalCodeSecondario());
        dto.setOrgName2(dto.getOrgNameSecondario());
        dto.setIban2(dto.getIbanSecondario());
        dto.setAddress2(dto.getAddressSecondario());
        dto.setCivic2(dto.getCivicSecondario());
        dto.setPostalCode2(dto.getPostalCodeSecondario());
        dto.setLocationSecondario(dto.getLocationSecondario());
        dto.setProvince2(dto.getProvinceSecondario());
        dto.setNation2(dto.getNationSecondario());
        dto.setRemittanceInformation2(dto.getRemittanceInformationSecondario());
        dto.setAmount2(dto.getAmountSecondario());
        dto.setCategory2(dto.getCategorySecondario());
      }

    }
}
