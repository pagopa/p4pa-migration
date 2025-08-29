package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;

public interface MigrationFileWfInvokerService {
  WorkflowCreatedDTO invokeWf(Uploads uploads);
}
