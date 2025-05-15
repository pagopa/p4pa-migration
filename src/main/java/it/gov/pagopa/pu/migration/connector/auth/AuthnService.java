package it.gov.pagopa.pu.migration.connector.auth;

public interface AuthnService {

  String getAccessToken();

  String getAccessToken(String orgIpaCode);
}
