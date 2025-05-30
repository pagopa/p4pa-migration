package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DataMigrationWfClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DebtPositionTypeOrgOperatorDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import org.springframework.stereotype.Service;

@Service
public class MigrationFileWfInvokerServiceImpl implements MigrationFileWfInvokerService {

  private final OrganizationDataMigrationWFClient organizationDataMigrationWFClient;
  private final DebtPositionTypeOrgOperatorDataMigrationWFClient debtPositionTypeOrgOperatorDataMigrationWFClient;

  public MigrationFileWfInvokerServiceImpl(OrganizationDataMigrationWFClient organizationDataMigrationWFClient, DebtPositionTypeOrgOperatorDataMigrationWFClient debtPositionTypeOrgOperatorDataMigrationWFClient) {
    this.organizationDataMigrationWFClient = organizationDataMigrationWFClient;
    this.debtPositionTypeOrgOperatorDataMigrationWFClient = debtPositionTypeOrgOperatorDataMigrationWFClient;
  }

  @Override
  public WorkflowCreatedDTO invokeWf(Uploads uploads) {
    DataMigrationWfClient wfClient = switch (uploads.getFileType()){
      case ORGANIZATIONS -> organizationDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS -> debtPositionTypeOrgOperatorDataMigrationWFClient;
    };

    return wfClient.migrate(uploads.getUploadId());
  }
}
