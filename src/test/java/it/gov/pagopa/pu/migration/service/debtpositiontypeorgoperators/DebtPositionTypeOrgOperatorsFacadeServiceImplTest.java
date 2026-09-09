package it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.migration.service.AuthorizationService;
import it.gov.pagopa.pu.migration.wf.mapper.DebtPositionTypeOrgOperatorMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsFacadeServiceImplTest {

  @Mock
  private DebtPositionTypeOrgOperatorMapper debtPositionTypeOrgOperatorMapperMock;
  @Mock
  private DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepositoryMock;
  @InjectMocks
  private DebtPositionTypeOrgOperatorsFacadeServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorMapperMock, debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void whenGetUnconsumedDebtPositionTypeOrgIdsThenReturnRepositoryResult() {
    // Given
    UserInfo loggedUser = new UserInfo();
    loggedUser.setUserId("user-123");
    loggedUser.setMappedExternalUserId("mappedExternalUserId");
    Long organizationId = 3L;
    String fiscalCode = "fiscalCode";
    byte[] fiscalCodeHash = "fiscalCode".getBytes();
    List<Long> expectedResult = List.of(10L, 30L);

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
      authorizationServiceMockedStatic
        .when(() -> AuthorizationService.validateAdminRole(organizationId, loggedUser))
        .thenAnswer(a -> null);
      when(debtPositionTypeOrgOperatorMapperMock.hashFiscalCode(fiscalCode)).thenReturn(fiscalCodeHash);
      when(debtPositionTypeOrgOperatorsRepositoryMock.findUnconsumedByOrganizationIdAndFiscalCodeHash(organizationId, fiscalCodeHash))
        .thenReturn(expectedResult);

      // When
      List<Long> result = service.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode, loggedUser);

      // Then
      Assertions.assertSame(expectedResult, result);
    }
  }
}
