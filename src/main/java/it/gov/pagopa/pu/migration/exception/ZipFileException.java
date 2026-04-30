package it.gov.pagopa.pu.migration.exception;

public class ZipFileException extends RuntimeException {
  public ZipFileException(String message, Throwable e) {
    super(message, e);
  }
}
