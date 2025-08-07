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
    private String flagPuPagoPaPayment;

  @CsvBindByName(column = "bilancio")
    private String balance;

  @CsvBindByName(column = "flagMultiBeneficiario")
    private String flagMultiBeneficiary;

  @CsvBindByName(column = "numeroBeneficiari")
    private String numberBeneficiary;

  @CsvBindByName(column = "codiceFiscaleEnteSecondario")
  private String orgFiscalCode2;

  @CsvBindByName(column = "denominazioneEnteSecondario")
  private String orgName2;

  @CsvBindByName(column = "ibanAccreditoEnteSecondario")
  private String iban2;

  @CsvBindByName(column = "indirizzoEnteSecondario")
  private String address2;

  @CsvBindByName(column = "civicoEnteSecondario")
  private String civic2;

  @CsvBindByName(column = "capEnteSecondario")
  private String postalCode2;

  @CsvBindByName(column = "localitaEnteSecondario")
  private String location2;

  @CsvBindByName(column = "provinciaEnteSecondario")
  private String province2;

  @CsvBindByName(column = "nazioneEnteSecondario")
  private String nation2;

  @CsvBindByName(column = "causaleVersamentoEnteSecondario")
  private String remittanceInformation2;

  @CsvBindByName(column = "importoVersamentoEnteSecondario")
  private String amount2;

  @CsvBindByName(column = "datiSpecificiRiscossioneEnteSecondario")
  private String category2;

    @CsvBindByName(column = "configurazioniEsecuzione")
    private String executionConfig;

    @CsvBindByName(column = "azione")
    private String action;

    @CsvBindByName(column = "draft")
    private String draft;
}
