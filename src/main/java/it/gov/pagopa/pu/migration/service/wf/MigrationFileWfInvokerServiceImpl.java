package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DataMigrationWfClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import org.springframework.stereotype.Service;

@Service
public class MigrationFileWfInvokerServiceImpl implements MigrationFileWfInvokerService {

  private final OrganizationDataMigrationWFClient organizationDataMigrationWFClient;

  public MigrationFileWfInvokerServiceImpl(OrganizationDataMigrationWFClient organizationDataMigrationWFClient) {
    this.organizationDataMigrationWFClient = organizationDataMigrationWFClient;
  }

  @Override
  public WorkflowCreatedDTO invokeWf(Uploads uploads) {
    DataMigrationWfClient wfClient = switch (uploads.getFileType()){
      case ORGANIZATIONS -> organizationDataMigrationWFClient;
    };

    return wfClient.migrate(uploads.getUploadId());
  }
}
