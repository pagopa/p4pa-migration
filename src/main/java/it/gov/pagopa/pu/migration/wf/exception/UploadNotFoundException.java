package it.gov.pagopa.pu.migration.wf.exception;

public class UploadNotFoundException extends NotRetryableActivityException {
  public UploadNotFoundException(String message) {
    super(message);
  }
}
