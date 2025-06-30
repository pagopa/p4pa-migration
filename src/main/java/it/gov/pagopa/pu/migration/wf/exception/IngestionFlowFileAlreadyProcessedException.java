package it.gov.pagopa.pu.migration.wf.exception;

public class IngestionFlowFileAlreadyProcessedException extends NotRetryableActivityException {
  public IngestionFlowFileAlreadyProcessedException(String message) {
    super(message);
  }
}
