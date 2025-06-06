package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.migration.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.migration.exception.InvalidAccessTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

  @InjectMocks
  private AuthorizationService authorizationService;
  @Mock
  private AuthnClient authClientImplMock;

  @Test
  void givenValidAccessTokenWhenValidateTokenThenOk() {
    UserInfo ui = new UserInfo();
    when(authClientImplMock.getUserInfo("ACCESSTOKEN")).thenReturn(ui);
    UserInfo result = authorizationService.validateToken("ACCESSTOKEN");

    Assertions.assertEquals(ui, result);
  }

  @Test
  void givenInvalidAccessTokenWhenValidateTokenThenInvalidAccessTokenException() {
    when(authClientImplMock.getUserInfo("INVALIDACCESSTOKEN")).thenThrow(new InvalidAccessTokenException("Bad Access Token provided"));
    InvalidAccessTokenException result = Assertions.assertThrows(InvalidAccessTokenException.class,
      () -> authorizationService.validateToken("INVALIDACCESSTOKEN"));

    Assertions.assertEquals("Bad Access Token provided", result.getMessage());
  }

  @Test
  void whenValidateAdminRoleOnBrokerOrgIdThenInvokeValidations(){
    // Given
    long organizationId = 1L;
    UserInfo loggedUser = new UserInfo();
    UserOrganizationRoles expectedResult = new UserOrganizationRoles();
    AuthorizationDeniedException expectedConfiguredException = new AuthorizationDeniedException("DUMMY");

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = Mockito.mockStatic(AuthorizationService.class)){
      authorizationServiceMockedStatic.when(() -> AuthorizationService.validateAdminRoleOnBroker(Mockito.same(organizationId), Mockito.same(loggedUser)))
        .thenCallRealMethod();

      authorizationServiceMockedStatic.when(() -> AuthorizationService.validateAdminRole(Mockito.same(organizationId), Mockito.same(loggedUser)))
        .thenReturn(expectedResult);

      //noinspection ThrowableNotThrown
      authorizationServiceMockedStatic.when(() -> AuthorizationService.getUnauthorizedOrgException(organizationId))
        .thenReturn(expectedConfiguredException);

      // When
      UserOrganizationRoles result = AuthorizationService.validateAdminRoleOnBroker(organizationId, loggedUser);

      // Then
      Assertions.assertSame(expectedResult, result);
      authorizationServiceMockedStatic.verify(() -> AuthorizationService.validateBrokerOrganization(Mockito.same(expectedResult), Mockito.same(loggedUser), Mockito.same(expectedConfiguredException)));
    }
  }

  @Test
  void whenValidateAdminRoleOnBrokerOrgIpaCodeThenInvokeValidations(){
    // Given
    String organizationIpaCode = "IPACODE";
    UserInfo loggedUser = new UserInfo();
    UserOrganizationRoles expectedResult = new UserOrganizationRoles();
    AuthorizationDeniedException expectedConfiguredException = new AuthorizationDeniedException("DUMMY");

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = Mockito.mockStatic(AuthorizationService.class)){
      authorizationServiceMockedStatic.when(() -> AuthorizationService.validateAdminRoleOnBroker(Mockito.same(organizationIpaCode), Mockito.same(loggedUser)))
        .thenCallRealMethod();

      authorizationServiceMockedStatic.when(() -> AuthorizationService.validateAdminRole(Mockito.same(organizationIpaCode), Mockito.same(loggedUser)))
        .thenReturn(expectedResult);

      //noinspection ThrowableNotThrown
      authorizationServiceMockedStatic.when(() -> AuthorizationService.getUnauthorizedOrgException(organizationIpaCode))
        .thenReturn(expectedConfiguredException);

      // When
      UserOrganizationRoles result = AuthorizationService.validateAdminRoleOnBroker(organizationIpaCode, loggedUser);

      // Then
      Assertions.assertSame(expectedResult, result);
      authorizationServiceMockedStatic.verify(() -> AuthorizationService.validateBrokerOrganization(Mockito.same(expectedResult), Mockito.same(loggedUser), Mockito.same(expectedConfiguredException)));
    }
  }

//region test on logged user authorizations
  @Test
  void givenAdminRoleWhenValidateAdminRoleOrgIdThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    AuthorizationService.validateAdminRole(1L,userInfo);
  }

  @Test
  void givenNoAdminRoleWhenValidateAdminRoleOrgIdThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> AuthorizationService.validateAdminRole(2L,userInfo));

    Assertions.assertEquals("Access denied on organizationId " + 2L + " to user externalUserId", result.getMessage());
  }
  @Test
  void givenAdminRoleWhenValidateAdminRoleOrgIpaCodeThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationIpaCode("IPACODE1");
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationIpaCode("IPACODE2");
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    AuthorizationService.validateAdminRole("IPACODE1",userInfo);
  }

  @Test
  void givenNoAdminRoleWhenValidateAdminRoleOrgIpaCodeThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationIpaCode("IPACODE1");
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationIpaCode("IPACODE1");
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> AuthorizationService.validateAdminRole("IPACODE2",userInfo));

    Assertions.assertEquals("Access denied on organizationIpaCode IPACODE2 to user externalUserId", result.getMessage());
  }

  @Test
  void givenAdminRoleWhenIsAdminRoleOrgIdThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    boolean adminRole = AuthorizationService.isAdminRole(1L, userInfo);

    Assertions.assertTrue(adminRole);
  }

  @Test
  void givenNoAdminRoleWhenIsAdminRoleOrgIdThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");
    boolean adminRole = AuthorizationService.isAdminRole(2L, userInfo);

    Assertions.assertFalse(adminRole);
  }

  @Test
  void givenAdminRoleWhenIsAdminRoleOrgIpaCodeThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationIpaCode("IPACODE1");
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationIpaCode("IPACODE2");
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    boolean adminRole = AuthorizationService.isAdminRole("IPACODE1", userInfo);

    Assertions.assertTrue(adminRole);
  }

  @Test
  void givenNoAdminRoleWhenIsAdminRoleOrgIpaCodeThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationIpaCode("IPACODE1");
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationIpaCode("IPACODE2");
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");
    boolean adminRole = AuthorizationService.isAdminRole("IPACODE2", userInfo);

    Assertions.assertFalse(adminRole);
  }

  @Test
  void givenUserEnabledToOrganizationIdWhenValidateUserForOrganizationIdThenOk() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of("TEST"));
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertDoesNotThrow(() -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @Test
  void givenUserNotEnabledToOrganizationIdWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of("TEST"));
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(2L, userInfo));
  }

  @Test
  void givenUserWithEmptyRolesWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of());
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @Test
  void givenUserWithNullRolesWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(null);
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @ParameterizedTest
  @CsvSource({
    "true, IPA_2, true",  // Valid admin user for the organization
    "true, IPA_1, false", // User without admin role for the organization
    "true, IPA_3, false", // Organization not associated with the user
    "false, IPA_2, false"  // Invalid user (no logged-in user)
  })
  void testIsAdminRole(boolean logged, String organizationIpaCode, boolean expectedResult) {
    // Given
    UserInfo expectedUserInfo = null;
    if (logged) {
      expectedUserInfo = new UserInfo();
      expectedUserInfo.setMappedExternalUserId("USERID");
      expectedUserInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of(AuthorizationService.ROLE_ADMIN))
      ));
    }

    // When
    boolean result = AuthorizationService.isAdminRole(organizationIpaCode, expectedUserInfo);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }


  @ParameterizedTest
  @CsvSource(value={
    "USERID, IPA_1, CF_1",  // Valid organization with fiscal code
    "USERID, IPA_2, CF_2",  // Another valid organization with fiscal code
    "USERID, IPA_3, null",  // Organization not associated with the user
    "null, IPA_1, null",    // Null user
    "USERID, null, null"    // Null organization IPA code
  }, nullValues={"null"})
  void testGetOrgFiscalCodeFromUserInfo(String userId, String organizationIpaCode, String expectedFiscalCode) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    String result = AuthorizationService.getOrgFiscalCodeFromUserInfo(userInfo, organizationIpaCode);

    // Then
    Assertions.assertEquals(expectedFiscalCode, result);
  }

  @ParameterizedTest
  @CsvSource(value={
    "USERID, IPA_1, 1",
    "USERID, IPA_2, 2",
    "USERID, IPA_3, null",
    "null, IPA_1, null",
    "USERID, null, null"
  }, nullValues={"null"})
  void testGetOrganizationIdFromUserInfo(String userId, String organizationIpaCode, Long expectedId) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    Long result = AuthorizationService.getOrganizationIdFromUserInfo(userInfo, organizationIpaCode);

    // Then
    Assertions.assertEquals(expectedId, result);
  }
//end region

//region test on logged user organization validation
  @Test
  void givenBrokerOrgWhenValidateBrokerOrganizationOrgIdThenOk() {
    // Given
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setOrganizationId(1L);
    userOrgRole.setOrganizationFiscalCode("ORGCF");
    userOrgRole.setRoles(List.of("ROLE"));

    UserInfo userInfo = new UserInfo();
    userInfo.setBrokerFiscalCode("ORGCF");
    userInfo.setOrganizations(List.of(userOrgRole));

    // When, Then
    Assertions.assertDoesNotThrow(() -> AuthorizationService.validateBrokerOrganization(1L, userInfo));
  }

  @Test
  void givenNoBrokerOrgWhenValidateBrokerOrganizationOrgIdThenOk() {
    // Given
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setOrganizationId(1L);
    userOrgRole.setOrganizationFiscalCode("ORGCF");
    userOrgRole.setRoles(List.of("ROLE"));

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateBrokerOrganization(1L, userInfo));

    // Then
    Assertions.assertEquals("Access denied the organizationId 1 is not a broker", result.getMessage());
  }

  @Test
  void givenBrokerOrgWhenValidateBrokerOrganizationOrgIpaCodeThenOk() {
    // Given
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setOrganizationIpaCode("IPACODE1");
    userOrgRole.setOrganizationFiscalCode("ORGCF");
    userOrgRole.setRoles(List.of("ROLE"));

    UserInfo userInfo = new UserInfo();
    userInfo.setBrokerFiscalCode("ORGCF");
    userInfo.setOrganizations(List.of(userOrgRole));

    // When, Then
    Assertions.assertDoesNotThrow(() -> AuthorizationService.validateBrokerOrganization("IPACODE1", userInfo));
  }

  @Test
  void givenNoBrokerOrgWhenValidateBrokerOrganizationOrgIpaCodeThenOk() {
    // Given
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setOrganizationIpaCode("IPACODE1");
    userOrgRole.setOrganizationFiscalCode("ORGCF");
    userOrgRole.setRoles(List.of("ROLE"));

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateBrokerOrganization("IPACODE1", userInfo));

    // Then
    Assertions.assertEquals("Access denied the organizationIpaCode IPACODE1 is not a broker", result.getMessage());
  }

  @Test
  void whenIsBrokerOrganizationOrgIdThenInvokeCommonCheck() {
    // Given
    Long orgId = 0L;
    UserInfo userInfo = new UserInfo();
    UserOrganizationRoles organizationRoles = new UserOrganizationRoles();
    boolean expectedResult = true;

    try (MockedStatic<AuthorizationService> authorizationServiceMockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
      authorizationServiceMockedStatic.when(() -> AuthorizationService.isBrokerOrganization(orgId, userInfo))
        .thenCallRealMethod();

      authorizationServiceMockedStatic.when(() -> AuthorizationService.getUserOrganizationRoles(orgId, userInfo))
        .thenReturn(Optional.of(organizationRoles));
      authorizationServiceMockedStatic.when(() -> AuthorizationService.isBrokerOrganization(organizationRoles, userInfo))
        .thenReturn(expectedResult);

      // When
      boolean result = AuthorizationService.isBrokerOrganization(orgId, userInfo);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }

  @Test
  void whenIsBrokerOrganizationOrgIpaCodeThenInvokeCommonCheck() {
    // Given
    String orgIpaCode = "IPACODE1";
    UserInfo userInfo = new UserInfo();
    UserOrganizationRoles organizationRoles = new UserOrganizationRoles();
    boolean expectedResult = true;

    try(MockedStatic<AuthorizationService> authorizationServiceMockedStatic = Mockito.mockStatic(AuthorizationService.class)) {
      authorizationServiceMockedStatic.when(() -> AuthorizationService.isBrokerOrganization(orgIpaCode, userInfo))
        .thenCallRealMethod();

      authorizationServiceMockedStatic.when(() -> AuthorizationService.getUserOrganizationRoles(orgIpaCode, userInfo))
          .thenReturn(Optional.of(organizationRoles));
      authorizationServiceMockedStatic.when(() -> AuthorizationService.isBrokerOrganization(organizationRoles, userInfo))
        .thenReturn(expectedResult);

      // When
      boolean result = AuthorizationService.isBrokerOrganization(orgIpaCode, userInfo);

      // Then
      Assertions.assertEquals(expectedResult, result);
    }
  }
//end region
}


