package it.gov.pagopa.pu.migration.exception;

public class InvalidFileException extends RuntimeException {

  public InvalidFileException(String message) {
    this(message, null);
  }

  public InvalidFileException(String message, Throwable e) {
    super(message, e);
  }
}
