package it.gov.pagopa.pu.migration.exception;

public class MigrationFileProcessingException extends RuntimeException {
    public MigrationFileProcessingException(String message) {
        super(message);
    }
    public MigrationFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

