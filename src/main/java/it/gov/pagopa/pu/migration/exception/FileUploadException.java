package it.gov.pagopa.pu.migration.exception;

public class FileUploadException extends RuntimeException {
  public FileUploadException(String message) {
    super(message);
  }

  public FileUploadException(String message, Throwable e) {
    super(message,e);
  }
}
