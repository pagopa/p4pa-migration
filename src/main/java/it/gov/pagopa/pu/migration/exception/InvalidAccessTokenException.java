package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

public class InvalidAccessTokenException extends BaseBusinessException {
  public InvalidAccessTokenException(String message) {
    super("INVALID_ACCESS_TOKEN", message);
  }
}
