package it.gov.pagopa.pu.migration.wf.exception;

public class IngestionFlowFileNotFoundException extends NotRetryableActivityException {
  public IngestionFlowFileNotFoundException(String message) {
    super(message);
  }
}
