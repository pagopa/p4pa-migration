package it.gov.pagopa.pu.migration.connector.debtposition.client;

import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.migration.connector.debtposition.config.DebtPositionApisHolder;
import it.gov.pagopa.pu.migration.exception.common.RestInvokeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgClientTest {

    @Mock
    private DebtPositionApisHolder debtPositionApisHolderMock;
    @Mock
    private DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchApiMock;
    @Mock
    private DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityApiMock;

    private DebtPositionTypeOrgClient debtPositionTypeOrgClient;

    @BeforeEach
    void setUp() {
        debtPositionTypeOrgClient = new DebtPositionTypeOrgClient(debtPositionApisHolderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                debtPositionApisHolderMock
        );
    }


	@Test
	void whenGetDebtPositionTypeOrgsFindByOrganizationIdAndCodeInvokeWithAccessToken() {
        // Given
        String accessToken = "ACCESSTOKEN";
        Long orgId = 0L;
        String code = "CODE";
        DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

        when(debtPositionApisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
                .thenReturn(debtPositionTypeOrgSearchApiMock);
        when(debtPositionTypeOrgSearchApiMock.crudDebtPositionTypeOrgsFindByOrganizationIdAndCode(orgId, code))
                .thenReturn(expectedResult);

        // When
        DebtPositionTypeOrg result = debtPositionTypeOrgClient.getDebtPositionTypeOrgByCodeAndOrgId(code, orgId, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
	}


  @Test
  void whenGetByIdThenInvokeWithAccessToken(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long debtPositionTypeOrgId = 0L;
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

    when(debtPositionApisHolderMock.getDebtPositionTypeOrgEntityApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityApiMock);
    when(debtPositionTypeOrgEntityApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
      .thenReturn(expectedResult);

    // When
    DebtPositionTypeOrg result = debtPositionTypeOrgClient.getById(debtPositionTypeOrgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentDebtPositionTypeOrgWhenGetByIdThenNull(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long debtPositionTypeOrgId = 0L;

    when(debtPositionApisHolderMock.getDebtPositionTypeOrgEntityApi(accessToken))
      .thenReturn(debtPositionTypeOrgEntityApiMock);
    when(debtPositionTypeOrgEntityApiMock.crudGetDebtpositiontypeorg(debtPositionTypeOrgId+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    DebtPositionTypeOrg result = debtPositionTypeOrgClient.getById(debtPositionTypeOrgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }


}
