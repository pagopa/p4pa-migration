package it.gov.pagopa.pu.migration.connector.debtposition;

import it.gov.pagopa.pu.migration.connector.debtposition.client.DebtPositionTypeOrgClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceTest {

    @Mock
    private DebtPositionTypeOrgClient debtPositionTypeOrgClientMock;

    private DebtPositionTypeOrgService debtPositionTypeOrgService;

    @BeforeEach
    void setUp() {
        debtPositionTypeOrgService = new DebtPositionTypeOrgServiceImpl(debtPositionTypeOrgClientMock);
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                debtPositionTypeOrgClientMock);
    }

    @Test
    void whenGetDebtPositionTypeOrgByCodeAndOrgIdThenInvokeClient() {
        // Given
        String accessToken = "ACCESSTOKEN";

        // When
        debtPositionTypeOrgService.getDebtPositionTypeOrgByCodeAndOrgId("code",1L,accessToken);

        // Then
        Mockito.verify(debtPositionTypeOrgClientMock).getDebtPositionTypeOrgByCodeAndOrgId("code",1L,accessToken);
    }
}
