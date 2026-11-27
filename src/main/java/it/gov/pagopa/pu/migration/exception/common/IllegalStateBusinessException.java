package it.gov.pagopa.pu.migration.exception.common;

import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;

@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class IllegalStateBusinessException extends NotRetryableActivityException {
  public IllegalStateBusinessException(String code, String message) {
    this(code, message, null);
  }

  public IllegalStateBusinessException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
