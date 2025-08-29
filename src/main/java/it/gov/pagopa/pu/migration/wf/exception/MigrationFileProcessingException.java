package it.gov.pagopa.pu.migration.wf.exception;

public class MigrationFileProcessingException extends NotRetryableActivityException {
  public MigrationFileProcessingException(String message) {
    super(message);
  }
}
