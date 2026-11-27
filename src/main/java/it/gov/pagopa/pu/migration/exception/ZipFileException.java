package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

public class ZipFileException extends BaseBusinessException {
  public ZipFileException(String message, Throwable e) {
    super("ZIP_FILE_ERROR", message, e);
  }
}
