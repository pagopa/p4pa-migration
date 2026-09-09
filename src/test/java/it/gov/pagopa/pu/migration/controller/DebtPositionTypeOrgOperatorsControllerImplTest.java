package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.security.SecurityUtilsTest;
import it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators.DebtPositionTypeOrgOperatorsFacadeService;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsControllerImplTest {
  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final UserInfo loggedUser = podamFactory.manufacturePojo(UserInfo.class);

  @Mock
  private DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeServiceMock;
  @InjectMocks
  private DebtPositionTypeOrgOperatorsControllerImpl controller;

  @BeforeEach
  void setUp() {
    SecurityUtilsTest.configureSecurityContext("accessToken", loggedUser);
  }

  @AfterEach
  void clearContextAndVerifyNoMoreInteractions() {
    SecurityUtilsTest.clearSecurityContext();
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsFacadeServiceMock);
  }

  @Test
  void givenCorrectRequestWhenGetUnconsumedDebtPositionTypeOrgIdsThenOk() {
    // Given
    long organizationId = 1L;
    String fiscalCode = "fiscalCode";
    List<Long> expectedResult = List.of(2L);

    when(debtPositionTypeOrgOperatorsFacadeServiceMock.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode, loggedUser))
      .thenReturn(expectedResult);

    // When
    ResponseEntity<List<Long>> response = controller.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode);

    // Then
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertSame(expectedResult, response.getBody());
  }
}
