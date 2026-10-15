package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

public class InvalidFileException extends BaseBusinessException {

  public InvalidFileException(String message) {
    this(message, null);
  }

  public InvalidFileException(String message, Throwable e) {
    super("INVALID_FILE", message, e);
  }
}
