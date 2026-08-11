package it.gov.pagopa.pu.migration.wf.exception;

import it.gov.pagopa.pu.migration.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

import java.util.List;

/** If thrown by an Activity, it cannot be retried */
public class NotRetryableActivityException extends BaseBusinessException {

    public NotRetryableActivityException(String code, String message, List<ErrorFieldDTO> fields, Throwable cause){
        super(code, message, fields, cause);
    }
    public NotRetryableActivityException(String code, String message, Throwable throwable){
        this(code, message, null, throwable);
    }

    public NotRetryableActivityException(String message, Throwable throwable){
        this("NOT_RETRYABLE_ERROR", message, throwable);
    }

    public NotRetryableActivityException(String code, String message){
        this(code, message, null);
    }

    public NotRetryableActivityException(String message){
        this(message, (Throwable) null);
    }
}
