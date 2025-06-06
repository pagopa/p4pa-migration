package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DataMigrationWfClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DebtPositionTypeDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DebtPositionTypeOrgOperatorDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.PaymentsReportingDataMigrationWFClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MigrationFileWfInvokerServiceImpl implements MigrationFileWfInvokerService {

  private final OrganizationDataMigrationWFClient organizationDataMigrationWFClient;
  private final DebtPositionTypeDataMigrationWFClient debtPositionTypeDataMigrationWFClient;
  private final DebtPositionTypeOrgOperatorDataMigrationWFClient debtPositionTypeOrgOperatorDataMigrationWFClient;
  private final PaymentsReportingDataMigrationWFClient paymentsReportingDataMigrationWFClient;

  @Override
  public WorkflowCreatedDTO invokeWf(Uploads uploads) {
    DataMigrationWfClient wfClient = switch (uploads.getFileType()){
      case ORGANIZATIONS -> organizationDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS -> debtPositionTypeOrgOperatorDataMigrationWFClient;
      case PAYMENTS_REPORTING -> paymentsReportingDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE -> debtPositionTypeDataMigrationWFClient;
    };

    return wfClient.migrate(uploads.getUploadId());
  }
}
