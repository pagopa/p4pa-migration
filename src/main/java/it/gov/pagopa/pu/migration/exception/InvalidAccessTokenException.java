package it.gov.pagopa.pu.migration.exception;

public class InvalidAccessTokenException extends RuntimeException {
  public InvalidAccessTokenException(String message) {
    super(message);
  }
}
