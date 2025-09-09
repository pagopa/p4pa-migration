package it.gov.pagopa.pu.migration.wf.dto.debtposition;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentIngestionFlowFileDTO {

  @CsvBindByName(column = "IUPD")
  private String iupdOrg;

  @CsvBindByName(column = "descrizionePosizioneDebitoria")
  private String description;

  @CsvBindByName(column = "dataValidita")
  private String validityDate;

  @CsvBindByName(column = "multiDebtor")
  private String multiDebtor;

  @CsvBindByName(column = "dataNotifica")
  private String notificationDate;

  @CsvBindByName(column = "indiceOpzionePagamento")
  private String paymentOptionIndex;

  @CsvBindByName(column = "tipoOpzionePagamento")
  private String paymentOptionType;

  @CsvBindByName(column = "descrizioneOpzionePagamento")
  private String paymentOptionDescription;

  @CsvBindByName(column = "iud")
  private String iud;

  @CsvBindByName(column = "codIUV")
  private String iuv;

  @CsvBindByName(column = "tipoIdentificativoUnivoco")
  private String entityType;

  @CsvBindByName(column = "codiceIdentificativoUnivoco")
  private String fiscalCode;

  @CsvBindByName(column = "anagraficaPagatore")
  private String fullName;

  @CsvBindByName(column = "indirizzoPagatore")
  private String address;

  @CsvBindByName(column = "civicoPagatore")
  private String civic;

  @CsvBindByName(column = "capPagatore")
  private String postalCode;

  @CsvBindByName(column = "localitaPagatore")
  private String location;

  @CsvBindByName(column = "provinciaPagatore")
  private String province;

  @CsvBindByName(column = "nazionePagatore")
  private String nation;

  @CsvBindByName(column = "emailPagatore")
  private String email;

  @CsvBindByName(column = "dataEsecuzionePagamento")
  private String dueDate;

  @CsvBindByName(column = "importoDovuto")
  private String amount;

  @CsvBindByName(column = "tipoDovuto")
  private String debtPositionTypeCode;

  @CsvBindByName(column = "causaleVersamento")
  private String remittanceInformation;

  @CsvBindByName(column = "datiSpecificiRiscossione")
  private String legacyPaymentMetadata;

  @CsvBindByName(column = "flagGeneraIuv")
  private String generateNotice;

  @CsvBindByName(column = "flagPuPagoPaPayment")
  private String flagPuPagoPaPayment;

  @CsvBindByName(column = "bilancio")
  private String balance;

  @CsvBindByName(column = "flagMultiBeneficiario")
  private String flagMultiBeneficiary;

  @CsvBindByName(column = "numeroBeneficiari")
  private String numberBeneficiary;

  @CsvBindByName(column = "codiceFiscaleEnteSecondario")
  private String orgFiscalCodeSecondario;

  @CsvBindByName(column = "denominazioneEnteSecondario")
  private String orgNameSecondario;

  @CsvBindByName(column = "ibanAccreditoEnteSecondario")
  private String ibanSecondario;

  @CsvBindByName(column = "indirizzoEnteSecondario")
  private String addressSecondario;

  @CsvBindByName(column = "civicoEnteSecondario")
  private String civicSecondario;

  @CsvBindByName(column = "capEnteSecondario")
  private String postalCodeSecondario;

  @CsvBindByName(column = "localitaEnteSecondario")
  private String locationSecondario;

  @CsvBindByName(column = "provinciaEnteSecondario")
  private String provinceSecondario;

  @CsvBindByName(column = "nazioneEnteSecondario")
  private String nationSecondario;

  @CsvBindByName(column = "causaleVersamentoEnteSecondario")
  private String remittanceInformationSecondario;

  @CsvBindByName(column = "importoVersamentoEnteSecondario")
  private String amountSecondario;

  @CsvBindByName(column = "datiSpecificiRiscossioneEnteSecondario")
  private String categorySecondario;

  @CsvBindByName(column = "codiceFiscaleEnte_2")
  private String orgFiscalCode2;

  @CsvBindByName(column = "denominazioneEnte_2")
  private String orgName2;

  @CsvBindByName(column = "ibanAccreditoEnte_2")
  private String iban2;

  @CsvBindByName(column = "indirizzoEnte_2")
  private String address2;

  @CsvBindByName(column = "civicoEnte_2")
  private String civic2;

  @CsvBindByName(column = "capEnte_2")
  private String postalCode2;

  @CsvBindByName(column = "LocalitaEnte_2")
  private String location2;

  @CsvBindByName(column = "ProvinciaEnte_2")
  private String province2;

  @CsvBindByName(column = "NazioneEnte_2")
  private String nation2;

  @CsvBindByName(column = "CausaleVersamentoEnte_2")
  private String remittanceInformation2;

  @CsvBindByName(column = "importoVersamentoEnte_2")
  private String amount2;

  @CsvBindByName(column = "codiceTassonomiaEnte_2")
  private String category2;

  @CsvBindByName(column = "codiceFiscaleEnte_3")
  private String orgFiscalCode3;

  @CsvBindByName(column = "denominazioneEnte_3")
  private String orgName3;

  @CsvBindByName(column = "ibanAccreditoEnte_3")
  private String iban3;

  @CsvBindByName(column = "indirizzoEnte_3")
  private String address3;

  @CsvBindByName(column = "civicoEnte_3")
  private String civic3;

  @CsvBindByName(column = "capEnte_3")
  private String postalCode3;

  @CsvBindByName(column = "LocalitaEnte_3")
  private String location3;

  @CsvBindByName(column = "ProvinciaEnte_3")
  private String province3;

  @CsvBindByName(column = "NazioneEnte_3")
  private String nation3;

  @CsvBindByName(column = "CausaleVersamentoEnte_3")
  private String remittanceInformation3;

  @CsvBindByName(column = "importoVersamentoEnte_3")
  private String amount3;

  @CsvBindByName(column = "codiceTassonomiaEnte_3")
  private String category3;

  @CsvBindByName(column = "codiceFiscaleEnte_4")
  private String orgFiscalCode4;

  @CsvBindByName(column = "denominazioneEnte_4")
  private String orgName4;

  @CsvBindByName(column = "ibanAccreditoEnte_4")
  private String iban4;

  @CsvBindByName(column = "indirizzoEnte_4")
  private String address4;

  @CsvBindByName(column = "civicoEnte_4")
  private String civic4;

  @CsvBindByName(column = "capEnte_4")
  private String postalCode4;

  @CsvBindByName(column = "LocalitaEnte_4")
  private String location4;

  @CsvBindByName(column = "ProvinciaEnte_4")
  private String province4;

  @CsvBindByName(column = "NazioneEnte_4")
  private String nation4;

  @CsvBindByName(column = "CausaleVersamentoEnte_4")
  private String remittanceInformation4;

  @CsvBindByName(column = "importoVersamentoEnte_4")
  private String amount4;

  @CsvBindByName(column = "codiceTassonomiaEnte_4")
  private String category4;

  @CsvBindByName(column = "codiceFiscaleEnte_5")
  private String orgFiscalCode5;

  @CsvBindByName(column = "denominazioneEnte_5")
  private String orgName5;

  @CsvBindByName(column = "ibanAccreditoEnte_5")
  private String iban5;

  @CsvBindByName(column = "indirizzoEnte_5")
  private String address5;

  @CsvBindByName(column = "civicoEnte_5")
  private String civic5;

  @CsvBindByName(column = "capEnte_5")
  private String postalCode5;

  @CsvBindByName(column = "LocalitaEnte_5")
  private String location5;

  @CsvBindByName(column = "ProvinciaEnte_5")
  private String province5;

  @CsvBindByName(column = "NazioneEnte_5")
  private String nation5;

  @CsvBindByName(column = "CausaleVersamentoEnte_5")
  private String remittanceInformation5;

  @CsvBindByName(column = "importoVersamentoEnte_5")
  private String amount5;

  @CsvBindByName(column = "codiceTassonomiaEnte_5")
  private String category5;

  @CsvBindByName(column = "configurazioniEsecuzione")
  private String executionConfig;

  @CsvBindByName(column = "azione")
  private String action;

  @CsvBindByName(column = "draft")
  private String draft;
}
