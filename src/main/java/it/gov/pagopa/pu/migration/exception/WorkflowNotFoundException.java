package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.exception.common.BaseBusinessException;

public class WorkflowNotFoundException extends BaseBusinessException {
  public WorkflowNotFoundException(String message) {
    super("WORKFLOW_NOT_FOUND", message);
  }
}
