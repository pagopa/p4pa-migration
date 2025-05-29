package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DataMigrationWfClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OperatorDebtPositionTypeOrgDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import org.springframework.stereotype.Service;

@Service
public class MigrationFileWfInvokerServiceImpl implements MigrationFileWfInvokerService {

  private final OrganizationDataMigrationWFClient organizationDataMigrationWFClient;
  private final OperatorDebtPositionTypeOrgDataMigrationWFClient operatorDebtPositionTypeOrgDataMigrationWFClient;

  public MigrationFileWfInvokerServiceImpl(OrganizationDataMigrationWFClient organizationDataMigrationWFClient, OperatorDebtPositionTypeOrgDataMigrationWFClient operatorDebtPositionTypeOrgDataMigrationWFClient) {
    this.organizationDataMigrationWFClient = organizationDataMigrationWFClient;
    this.operatorDebtPositionTypeOrgDataMigrationWFClient = operatorDebtPositionTypeOrgDataMigrationWFClient;
  }

  @Override
  public WorkflowCreatedDTO invokeWf(Uploads uploads) {
    DataMigrationWfClient wfClient = switch (uploads.getFileType()){
      case ORGANIZATIONS -> organizationDataMigrationWFClient;
      case OPERATOR_DEBT_POSITION_TYPE_ORG -> operatorDebtPositionTypeOrgDataMigrationWFClient;
    };

    return wfClient.migrate(uploads.getUploadId());
  }
}
