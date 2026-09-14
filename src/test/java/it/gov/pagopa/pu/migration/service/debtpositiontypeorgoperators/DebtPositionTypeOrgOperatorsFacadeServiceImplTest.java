package it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.ConsumeDebtPositionTypeOrgOperatorsDTO;
import it.gov.pagopa.pu.migration.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.migration.service.AuthorizationService;
import it.gov.pagopa.pu.migration.utils.Utilities;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

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

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = mockStatic(AuthorizationService.class)) {
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

  @Test
  void whenConsumeDebtPositionTypeOrgOperatorsThenOk() {
    // Given
    UserInfo loggedUser = new UserInfo();
    loggedUser.setUserId("user-123");
    loggedUser.setMappedExternalUserId("mappedExternalUserId");
    Long organizationId = 3L;
    String fiscalCode = "fiscalCode";
    byte[] fiscalCodeHash = "fiscalCode".getBytes();
    List<Long> debtPositionTypeOrgIds = List.of(10L, 30L);
    OffsetDateTime consumptionDateTime = OffsetDateTime.of(2026,9,10,12,0,0,0, ZoneOffset.UTC);
    ConsumeDebtPositionTypeOrgOperatorsDTO consumeDTO = new ConsumeDebtPositionTypeOrgOperatorsDTO();
    consumeDTO.setDebtPositionTypeOrgIds(debtPositionTypeOrgIds);

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = mockStatic(AuthorizationService.class);
         MockedStatic<OffsetDateTime> offsetDateTimeMockedStatic = mockStatic(OffsetDateTime.class)) {
      authorizationServiceMockedStatic
        .when(() -> AuthorizationService.validateAdminRole(organizationId, loggedUser))
        .thenAnswer(a -> null);
      offsetDateTimeMockedStatic.when(() -> OffsetDateTime.now(Utilities.ZONEID)).thenReturn(consumptionDateTime);
      when(debtPositionTypeOrgOperatorMapperMock.hashFiscalCode(fiscalCode)).thenReturn(fiscalCodeHash);
      doNothing().when(debtPositionTypeOrgOperatorsRepositoryMock)
        .consumeDebtPositionTypeOrgOperators(organizationId, fiscalCodeHash, debtPositionTypeOrgIds, consumptionDateTime);

      // When
      service.consumeDebtPositionTypeOrgOperators(organizationId, fiscalCode, consumeDTO, loggedUser);
    }
  }

  @Test
  void givenEmptyDebtPositionTypeOrgIdsWhenConsumeDebtPositionTypeOrgOperatorsThenNoConsume() {
    // Given
    UserInfo loggedUser = new UserInfo();
    loggedUser.setUserId("user-123");
    loggedUser.setMappedExternalUserId("mappedExternalUserId");
    Long organizationId = 3L;
    String fiscalCode = "fiscalCode";
    ConsumeDebtPositionTypeOrgOperatorsDTO consumeDTO = new ConsumeDebtPositionTypeOrgOperatorsDTO();
    consumeDTO.setDebtPositionTypeOrgIds(Collections.emptyList());

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = mockStatic(AuthorizationService.class)) {
      authorizationServiceMockedStatic
        .when(() -> AuthorizationService.validateAdminRole(organizationId, loggedUser))
        .thenAnswer(a -> null);

      // When
      service.consumeDebtPositionTypeOrgOperators(organizationId, fiscalCode, consumeDTO, loggedUser);

      // Then
      authorizationServiceMockedStatic.verify(() -> AuthorizationService.validateAdminRole(organizationId, loggedUser));
    }
  }
}
