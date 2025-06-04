package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.migration.connector.auth.client.AuthnClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Service
@Slf4j
public class AuthorizationService {
  public static final String ROLE_ADMIN = "ROLE_ADMIN";

  private final AuthnClient authClientImpl;

  public AuthorizationService(AuthnClient authClientImpl) {
    this.authClientImpl = authClientImpl;
  }

  public UserInfo validateToken(String accessToken) {
    log.info("Requesting validate token");
    return authClientImpl.getUserInfo(accessToken);
  }

  public static void validateAdminRoleOnBroker(Long organizationId, UserInfo loggedUser) {
    validateAdminRole(organizationId, loggedUser);
    validateBrokerOrganization(organizationId, loggedUser);
  }

  public static void validateAdminRoleOnBroker(String organizationIpaCode, UserInfo loggedUser) {
    validateAdminRole(organizationIpaCode, loggedUser);
    validateBrokerOrganization(organizationIpaCode, loggedUser);
  }

//region logged user authorizations
  public static void validateAdminRole(Long organizationId, UserInfo loggedUser) {
    boolean roleAdmin = isAdminRole(organizationId, loggedUser);
    if (!roleAdmin) {
      handleUnauthorizedUser(organizationId, loggedUser);
    }
  }

  public static void validateAdminRole(String organizationIpaCode, UserInfo loggedUser) {
    boolean roleAdmin = isAdminRole(organizationIpaCode, loggedUser);
    if (!roleAdmin) {
      handleUnauthorizedUser(organizationIpaCode, loggedUser);
    }
  }

  public static boolean isAdminRole(Long organizationId, UserInfo loggedUser) {
    return getUserOrganizationRoles(organizationId, loggedUser)
      .filter(o -> !CollectionUtils.isEmpty(o.getRoles()) && o.getRoles()
        .contains(ROLE_ADMIN))
      .isPresent();
  }

  public static boolean isAdminRole(String organizationIpaCode, UserInfo loggedUser) {
    return loggedUser != null && getUserOrganizationRoles(organizationIpaCode, loggedUser)
      .filter(o -> !CollectionUtils.isEmpty(o.getRoles()) && o.getRoles()
        .contains(ROLE_ADMIN))
      .isPresent();
  }

  public static void validateUserForOrganizationId(Long organizationId, UserInfo loggedUser) {
    if (getUserOrganizationRoles(organizationId, loggedUser).isEmpty()) {
      handleUnauthorizedUser(organizationId, loggedUser);
    }
  }

  public static String getOrgFiscalCodeFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if(loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
  }

  public static Long getOrganizationIdFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if(loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationId)
      .orElse(null);
  }

  private static void handleUnauthorizedUser(Long organizationId, UserInfo loggedUser) {
    log.debug("Unauthorized user. [organizationId:{}]", organizationId);
    throw new AuthorizationDeniedException("Access denied on organizationId " + organizationId + " to user " + loggedUser.getMappedExternalUserId());
  }

  private static void handleUnauthorizedUser(String organizationIpaCode, UserInfo loggedUser) {
    log.debug("Unauthorized user. [organizationIpaCode:{}]", organizationIpaCode);
    throw new AuthorizationDeniedException("Access denied on organizationIpaCode " + organizationIpaCode + " to user " + loggedUser.getMappedExternalUserId());
  }

  private static Optional<UserOrganizationRoles> getUserOrganizationRoles(Long organizationId, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationId.equals(o.getOrganizationId()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }

  public static Optional<UserOrganizationRoles> getUserOrganizationRoles(String organizationIpaCode, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationIpaCode.equals(o.getOrganizationIpaCode()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }
//endregion

//region logged user organization validation
  public static void validateBrokerOrganization(Long organizationId, UserInfo loggedUser) {
    boolean isBrokerOrg = isBrokerOrganization(organizationId, loggedUser);
    if (!isBrokerOrg) {
      handleUnauthorizedOrg(organizationId);
    }
  }

  public static void validateBrokerOrganization(String organizationIpaCode, UserInfo loggedUser) {
    boolean isBrokerOrg = isBrokerOrganization(organizationIpaCode, loggedUser);
    if (!isBrokerOrg) {
      handleUnauthorizedOrg(organizationIpaCode);
    }
  }

  public static boolean isBrokerOrganization(Long organizationId, UserInfo loggedUser) {
    String orgFiscalCode = getUserOrganizationRoles(organizationId, loggedUser)
      .map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
    return loggedUser.getBrokerFiscalCode() !=null && loggedUser.getBrokerFiscalCode().equals(orgFiscalCode);
  }

  public static boolean isBrokerOrganization(String organizationIpaCode, UserInfo loggedUser) {
    String orgFiscalCode = getUserOrganizationRoles(organizationIpaCode, loggedUser)
      .map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
    return loggedUser.getBrokerFiscalCode() !=null && loggedUser.getBrokerFiscalCode().equals(orgFiscalCode);
  }

  private static void handleUnauthorizedOrg(Long organizationId) {
    log.debug("Unauthorized organization. [organizationId:{}]", organizationId);
    throw new AuthorizationDeniedException("Access denied the organizationId " + organizationId + " is not a broker");
  }

  private static void handleUnauthorizedOrg(String organizationIpaCode) {
    log.debug("Unauthorized organization. [organizationIpaCode:{}]", organizationIpaCode);
    throw new AuthorizationDeniedException("Access denied the organizationIpaCode " + organizationIpaCode + " is not a broker");
  }
//endregion

}
