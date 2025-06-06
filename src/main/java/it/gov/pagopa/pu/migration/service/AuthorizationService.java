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

  public static UserOrganizationRoles validateAdminRoleOnBroker(Long organizationId, UserInfo loggedUser) {
    UserOrganizationRoles userOrganizationRoles = validateAdminRole(organizationId, loggedUser);
    validateBrokerOrganization(userOrganizationRoles, loggedUser, getUnauthorizedOrgException(organizationId));
    return userOrganizationRoles;
  }

  public static UserOrganizationRoles validateAdminRoleOnBroker(String organizationIpaCode, UserInfo loggedUser) {
    UserOrganizationRoles userOrganizationRoles = validateAdminRole(organizationIpaCode, loggedUser);
    validateBrokerOrganization(userOrganizationRoles, loggedUser, getUnauthorizedOrgException(organizationIpaCode));
    return userOrganizationRoles;
  }

  //region logged user authorizations
  public static UserOrganizationRoles validateAdminRole(Long organizationId, UserInfo loggedUser) {
    UserOrganizationRoles orgRoles = getUserOrganizationRoles(organizationId, loggedUser).orElse(null);
    return validateAdminRole(orgRoles, getUnauthorizedUserException(organizationId, loggedUser));
  }

  public static UserOrganizationRoles validateAdminRole(String organizationIpaCode, UserInfo loggedUser) {
    UserOrganizationRoles orgRoles = getUserOrganizationRoles(organizationIpaCode, loggedUser).orElse(null);
    return validateAdminRole(orgRoles, getUnauthorizedUserException(organizationIpaCode, loggedUser));
  }

  public static UserOrganizationRoles validateAdminRole(UserOrganizationRoles orgRoles, AuthorizationDeniedException authorizationDeniedException) {
    boolean roleAdmin = isAdminRole(orgRoles);
    if (!roleAdmin) {
      throw authorizationDeniedException;
    }
    return orgRoles;
  }

  public static boolean isAdminRole(Long organizationId, UserInfo loggedUser) {
    return isAdminRole(getUserOrganizationRoles(organizationId, loggedUser).orElse(null));
  }

  public static boolean isAdminRole(String organizationIpaCode, UserInfo loggedUser) {
    return loggedUser != null &&
      isAdminRole(getUserOrganizationRoles(organizationIpaCode, loggedUser).orElse(null));
  }

  public static boolean isAdminRole(UserOrganizationRoles o) {
    return o != null &&
      !CollectionUtils.isEmpty(o.getRoles()) &&
      o.getRoles().contains(ROLE_ADMIN);
  }

  public static void validateUserForOrganizationId(Long organizationId, UserInfo loggedUser) {
    if (getUserOrganizationRoles(organizationId, loggedUser).isEmpty()) {
      throw getUnauthorizedUserException(organizationId, loggedUser);
    }
  }

  public static String getOrgFiscalCodeFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if (loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
  }

  public static Long getOrganizationIdFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if (loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationId)
      .orElse(null);
  }

  private static AuthorizationDeniedException getUnauthorizedUserException(Long organizationId, UserInfo loggedUser) {
    return new AuthorizationDeniedException("Access denied on organizationId " + organizationId + " to user " + loggedUser.getMappedExternalUserId());
  }

  private static AuthorizationDeniedException getUnauthorizedUserException(String organizationIpaCode, UserInfo loggedUser) {
    return new AuthorizationDeniedException("Access denied on organizationIpaCode " + organizationIpaCode + " to user " + loggedUser.getMappedExternalUserId());
  }

  public static Optional<UserOrganizationRoles> getUserOrganizationRoles(Long organizationId, UserInfo loggedUser) {
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
  public static UserOrganizationRoles validateBrokerOrganization(Long organizationId, UserInfo loggedUser) {
    UserOrganizationRoles orgRoles = getUserOrganizationRoles(organizationId, loggedUser).orElse(null);
    validateBrokerOrganization(orgRoles, loggedUser, getUnauthorizedOrgException(organizationId));
    return orgRoles;
  }

  public static UserOrganizationRoles validateBrokerOrganization(String organizationIpaCode, UserInfo loggedUser) {
    UserOrganizationRoles orgRoles = getUserOrganizationRoles(organizationIpaCode, loggedUser).orElse(null);
    validateBrokerOrganization(orgRoles, loggedUser, getUnauthorizedOrgException(organizationIpaCode));
    return orgRoles;
  }

  public static void validateBrokerOrganization(UserOrganizationRoles orgRoles, UserInfo loggedUser, AuthorizationDeniedException authorizationDeniedException) {
    boolean isBrokerOrg = isBrokerOrganization(orgRoles, loggedUser);
    if (!isBrokerOrg) {
      throw authorizationDeniedException;
    }
  }

  public static boolean isBrokerOrganization(Long organizationId, UserInfo loggedUser) {
    return isBrokerOrganization(getUserOrganizationRoles(organizationId, loggedUser).orElse(null), loggedUser);
  }

  public static boolean isBrokerOrganization(String organizationIpaCode, UserInfo loggedUser) {
    return isBrokerOrganization(getUserOrganizationRoles(organizationIpaCode, loggedUser).orElse(null), loggedUser);
  }

  public static boolean isBrokerOrganization(UserOrganizationRoles orgRoles, UserInfo loggedUser) {
    return orgRoles != null &&
      loggedUser != null &&
      loggedUser.getBrokerFiscalCode() != null &&
      loggedUser.getBrokerFiscalCode().equals(orgRoles.getOrganizationFiscalCode());
  }

  protected static AuthorizationDeniedException getUnauthorizedOrgException(Long organizationId) {
    return new AuthorizationDeniedException("Access denied the organizationId " + organizationId + " is not a broker");
  }

  protected static AuthorizationDeniedException getUnauthorizedOrgException(String organizationIpaCode) {
    return new AuthorizationDeniedException("Access denied the organizationIpaCode " + organizationIpaCode + " is not a broker");
  }
//endregion

}
