package it.gov.pagopa.pu.migration.exception;

public class WorkflowNotFoundException extends RuntimeException {
  public WorkflowNotFoundException(String message) {
    super(message);
  }
}
