package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MigrationFileWfInvokerServiceImpl implements MigrationFileWfInvokerService {

  private final OrganizationDataMigrationWFClient organizationDataMigrationWFClient;
  private final DebtPositionTypeDataMigrationWFClient debtPositionTypeDataMigrationWFClient;
  private final DebtPositionTypeOrgOperatorDataMigrationWFClient debtPositionTypeOrgOperatorDataMigrationWFClient;
  private final PaymentNotificationDataMigrationWFClient paymentNotificationDataMigrationWFClient;
  private final PaymentsReportingDataMigrationWFClient paymentsReportingDataMigrationWFClient;
  private final TreasuryCsvCompleteDataMigrationWFClient treasuryCsvCompleteDataMigrationWFClient;
  private final AssessmentsRegistryDataMigrationWFClient assessmentsRegistryDataMigrationWFClient;
  private final DebtPositionTypeOrgDataMigrationWFClient debtPositionTypeOrgDataMigrationWFClient;

  @Override
  public WorkflowCreatedDTO invokeWf(Uploads uploads) {
    DataMigrationWfClient wfClient = switch (uploads.getFileType()) {
      case ORGANIZATIONS -> organizationDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS ->
        debtPositionTypeOrgOperatorDataMigrationWFClient;
      case PAYMENT_NOTIFICATION -> paymentNotificationDataMigrationWFClient;
      case PAYMENTS_REPORTING -> paymentsReportingDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE -> debtPositionTypeDataMigrationWFClient;
      case TREASURY_CSV_COMPLETE -> treasuryCsvCompleteDataMigrationWFClient;
      case ASSESSMENTS_REGISTRY -> assessmentsRegistryDataMigrationWFClient;
      case DEBT_POSITIONS_TYPE_ORG -> debtPositionTypeOrgDataMigrationWFClient;
    };

    return wfClient.migrate(uploads.getUploadId());
  }
}
