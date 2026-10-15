package it.gov.pagopa.pu.migration.exception.common;

import it.gov.pagopa.pu.migration.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;

import java.util.List;

@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class InvalidValueException extends NotRetryableActivityException {

  public InvalidValueException(String code, String message) {
    this(code, message, null, null);
  }

  public InvalidValueException(String code, String message, List<ErrorFieldDTO> fieldErrors) {
    this(code, message, fieldErrors, null);
  }

  public InvalidValueException(String code, String message, Throwable cause) {
    this(code, message, null, cause);
  }

  public InvalidValueException(String code, String message, List<ErrorFieldDTO> fieldErrors, Throwable cause) {
    super(code, message, fieldErrors, cause);
  }
}
