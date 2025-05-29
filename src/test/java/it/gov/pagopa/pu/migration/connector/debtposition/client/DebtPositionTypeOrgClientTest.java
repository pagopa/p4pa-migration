package it.gov.pagopa.pu.migration.connector.debtposition.client;

import it.gov.pagopa.pu.debtposition.client.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.migration.connector.debtposition.config.DebtPositionApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgClientTest {

    @Mock
    private DebtPositionApisHolder debtPositionApisHolderMock;
    @Mock
    private DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchApiMock;

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
	void whenGetDebtPositionTypeOrgByInstallmentIdInvokeWithAccessToken() {
        // Given
        String accessToken = "ACCESSTOKEN";
        Long orgId = 0L;
        String code = "CODE";
        DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();

        Mockito.when(debtPositionApisHolderMock.getDebtPositionTypeOrgSearchControllerApi(accessToken))
                .thenReturn(debtPositionTypeOrgSearchApiMock);
        Mockito.when(debtPositionTypeOrgSearchApiMock.crudDebtPositionTypeOrgsFindByOrganizationIdAndCode(orgId, code))
                .thenReturn(expectedResult);

        // When
        DebtPositionTypeOrg result = debtPositionTypeOrgClient.getDebtPositionTypeOrgByCodeAndOrgId(code, orgId, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
	}

}
