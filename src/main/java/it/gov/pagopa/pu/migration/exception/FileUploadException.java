package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

public class FileUploadException extends BaseBusinessException {
  public FileUploadException(String message) {
    this(message, null);
  }

  public FileUploadException(String message, Throwable e) {
    super("FILE_UPLOAD_ERROR", message,e);
  }
}
