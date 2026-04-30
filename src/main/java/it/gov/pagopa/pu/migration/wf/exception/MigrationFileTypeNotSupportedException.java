package it.gov.pagopa.pu.migration.wf.exception;

public class MigrationFileTypeNotSupportedException extends NotRetryableActivityException {
  public MigrationFileTypeNotSupportedException(String message) {
    super(message);
  }
}
