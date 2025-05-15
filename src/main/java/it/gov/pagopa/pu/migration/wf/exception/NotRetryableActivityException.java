package it.gov.pagopa.pu.migration.wf.exception;

/** If thrown by an Activity, it cannot be retried */
public class NotRetryableActivityException extends RuntimeException {

    public NotRetryableActivityException(String message, Throwable throwable){
        super(message, throwable);
    }

    public NotRetryableActivityException(String message){
        super(message);
    }
}
