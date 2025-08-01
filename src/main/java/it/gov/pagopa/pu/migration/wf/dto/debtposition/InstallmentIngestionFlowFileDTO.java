package it.gov.pagopa.pu.migration.wf.dto.debtposition;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import it.gov.pagopa.pu.debtposition.dto.generated.PersonEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;

import java.math.BigDecimal;
import java.time.LocalDate;

import static it.gov.pagopa.pu.migration.wf.dto.debtposition.InstallmentIngestionFlowFileVersions.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentIngestionFlowFileDTO {

    @CsvBindByName(column = "IUPD", profiles = V2_0)
    @CsvBindByName(column = "iupdOrg", profiles = V2_0_ENG)
    private String iupdOrg;

    @CsvBindByName(column = "descrizionePosizioneDebitoria", required = true, profiles = V2_0)
    @CsvBindByName(column = "description", required = true, profiles = V2_0_ENG)
    private String description;

    @CsvBindByName(column = "dataValidita", profiles = {V1_4, V2_0})
    @CsvBindByName(column = "validityDate", profiles = V2_0_ENG)
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate validityDate;

    @CsvBindByName(column = "multiDebtor", profiles = {V2_0, V2_0_ENG})
    private Boolean multiDebtor;

    @CsvBindByName(column = "dataNotifica", profiles = V2_0)
    @CsvBindByName(column = "notificationDate", profiles = V2_0_ENG)
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate notificationDate;

    @CsvBindByName(column = "indiceOpzionePagamento", required = true, profiles = {V1_4, V2_0})
    @CsvBindByName(column = "paymentOptionIndex", required = true, profiles = V2_0_ENG)
    private Integer paymentOptionIndex;

    @CsvBindByName(column = "tipoOpzionePagamento", required = true, profiles = V2_0)
    @CsvBindByName(column = "paymentOptionType", required = true, profiles = V2_0_ENG)
    private String paymentOptionType;

    @CsvBindByName(column = "descrizioneOpzionePagamento", profiles = V2_0)
    @CsvBindByName(column = "paymentOptionDescription", profiles = V2_0_ENG)
    private String paymentOptionDescription;

    @CsvBindByName(column = "iud", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0, V2_0_ENG})
    private String iud;

    @CsvBindByName(column = "codIUV", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "iuv", profiles = V2_0_ENG)
    private String iuv;

    @CsvBindByName(column = "tipoIdentificativoUnivoco", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "entityType", profiles = V2_0_ENG)
    private PersonEntityType entityType;

    @CsvBindByName(column = "codiceIdentificativoUnivoco", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "fiscalCode", required = true, profiles = V2_0_ENG)
    private String fiscalCode;

    @CsvBindByName(column = "anagraficaPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "fullName", required = true, profiles = V2_0_ENG)
    private String fullName;

    @CsvBindByName(column = "indirizzoPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "address", required = true, profiles = V2_0_ENG)
    private String address;

    @CsvBindByName(column = "civicoPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "civic", required = true, profiles = V2_0_ENG)
    private String civic;

    @CsvBindByName(column = "capPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "postalCode", required = true, profiles = V2_0_ENG)
    private String postalCode;

    @CsvBindByName(column = "localitaPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "location", required = true, profiles = V2_0_ENG)
    private String location;

    @CsvBindByName(column = "provinciaPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "province", required = true, profiles = V2_0_ENG)
    private String province;

    @CsvBindByName(column = "nazionePagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "nation", required = true, profiles = V2_0_ENG)
    private String nation;

    @CsvBindByName(column = "emailPagatore", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "email", required = true, profiles = V2_0_ENG)
    private String email;

    @CsvBindByName(column = "dataEsecuzionePagamento", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "dueDate", profiles = V2_0_ENG)
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate dueDate;

    @CsvBindByName(column = "importoDovuto", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "amount", profiles = V2_0_ENG)
    private BigDecimal amount;

    @CsvBindByName(column = "tipoDovuto", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "debtPositionTypeCode", required = true, profiles = V2_0_ENG)
    private String debtPositionTypeCode;


    @CsvBindByName(column = "causaleVersamento", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "remittanceInformation", required = true, profiles = V2_0_ENG)
    private String remittanceInformation;

    @CsvBindByName(column = "datiSpecificiRiscossione", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "legacyPaymentMetadata", profiles = V2_0_ENG)
    private String legacyPaymentMetadata;

    @CsvBindByName(column = "flagGeneraIuv", required = true, profiles = {V1_3, V1_4, V2_0})
    @CsvBindByName(column = "flagPuPagoPaPayment", required = true, profiles = V2_0_ENG)
    private Boolean flagPuPagoPaPayment;

    @CsvBindByName(column = "bilancio", profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "balance", profiles = V2_0_ENG)
    private String balance;

    @CsvBindByName(column = "flagMultiBeneficiario", profiles = {V1_4, V2_0})
    @CsvBindByName(column = "flagMultiBeneficiary", profiles = V2_0_ENG)
    private Boolean flagMultiBeneficiary;

    @CsvBindByName(column = "numeroBeneficiari", profiles = V2_0)
    @CsvBindByName(column = "numberBeneficiary", profiles = V2_0_ENG)
    private Integer numberBeneficiary;

    @CsvBindAndJoinByName(column = ".*_2", elementType = String.class, profiles = {V1_4, V2_0, V2_0_ENG})
    private MultiValuedMap<String, String> transfer2;

    @CsvBindAndJoinByName(column = ".*_3", elementType = String.class, profiles = {V2_0, V2_0_ENG})
    private MultiValuedMap<String, String> transfer3;

    @CsvBindAndJoinByName(column = ".*_4", elementType = String.class, profiles = {V2_0, V2_0_ENG})
    private MultiValuedMap<String, String> transfer4;

    @CsvBindAndJoinByName(column = ".*_5", elementType = String.class, profiles = {V2_0, V2_0_ENG})
    private MultiValuedMap<String, String> transfer5;

    @CsvBindByName(column = "configurazioniEsecuzione", profiles = V2_0)
    @CsvBindByName(column = "executionConfig", profiles = V2_0_ENG)
    private String executionConfig;

    @CsvBindByName(column = "azione", required = true, profiles = {V1_0, V1_1, V1_2, V1_3, V1_4, V2_0})
    @CsvBindByName(column = "action", required = true, profiles = V2_0_ENG)
    private Action action;

    @CsvBindByName(column = "draft", profiles = {V2_0, V2_0_ENG})
    private Boolean draft;
}
