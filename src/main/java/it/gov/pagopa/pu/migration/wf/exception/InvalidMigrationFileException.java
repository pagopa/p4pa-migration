package it.gov.pagopa.pu.migration.wf.exception;


/**
 * A custom exception that indicates an invalid migration file encountered
 * during the application's processing operations.
 *
 */
public class InvalidMigrationFileException extends NotRetryableActivityException {

	/**
	 * Constructs a new {@code InvalidMigrationFileException} with the specified detail message.
	 *
	 * @param message the detail message explaining the cause of the exception.
	 */
	public InvalidMigrationFileException(String message) {
		super(message);
	}
}

