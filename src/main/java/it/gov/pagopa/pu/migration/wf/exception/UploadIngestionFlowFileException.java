package it.gov.pagopa.pu.migration.wf.exception;

public class UploadIngestionFlowFileException extends NotRetryableActivityException {
  public UploadIngestionFlowFileException(String message) {
    super(message);
  }
}
