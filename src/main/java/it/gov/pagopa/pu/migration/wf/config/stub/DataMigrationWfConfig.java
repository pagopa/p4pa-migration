package it.gov.pagopa.pu.migration.wf.config.stub;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.migration.wf.activity.IngestionFlowFileRetrieverActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadDetailsUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessments.AssessmentsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessmentsregistry.AssessmentsRegistryMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositionspaid.DebtPositionsPaidMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontype.DebtPositionTypeMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorg.DebtPositionTypeOrgMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator.DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.orgsilservice.OrgSilServiceMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentnotification.PaymentNotificationMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentsreporting.PaymentsReportingMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.treasury.csvcomplete.TreasuryCsvCompleteFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.BaseWfConfig;
import it.gov.pagopa.pu.migration.wf.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.migration-data-ingestion")
public class DataMigrationWfConfig extends BaseWfConfig {

  public UploadsStatusUpdateActivity buildUploadsStatusUpdateActivityStub() {
    return Workflow.newActivityStub(UploadsStatusUpdateActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrganizationsMigrationFileTypeHandlerActivity buildOrganizationMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(OrganizationsMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrgSilServiceMigrationFileTypeHandlerActivity buildOrgSilServiceMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(OrgSilServiceMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public AssessmentsMigrationFileTypeHandlerActivity buildAssessmentsMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(AssessmentsMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public AssessmentsRegistryMigrationFileTypeHandlerActivity buildAssessmentsRegistryMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(AssessmentsRegistryMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PaymentNotificationMigrationFileTypeHandlerActivity buildPaymentNotificationMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(PaymentNotificationMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PaymentsReportingMigrationFileTypeHandlerActivity buildPaymentsReportingMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(PaymentsReportingMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DebtPositionTypeMigrationFileTypeHandlerActivity buildDebtPositionTypeMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(DebtPositionTypeMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DebtPositionTypeOrgMigrationFileTypeHandlerActivity buildDebtPositionTypeOrgMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(DebtPositionTypeOrgMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity buildDebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public TreasuryCsvCompleteFileTypeHandlerActivity buildTreasuryCsvCompleteMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(TreasuryCsvCompleteFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DebtPositionsPaidMigrationFileTypeHandlerActivity buildDebtPositionsPaidMigrationFileTypeHandlerActivityStub() {
    return Workflow.newActivityStub(DebtPositionsPaidMigrationFileTypeHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public IngestionFlowFileRetrieverActivity buildIngestionFlowFileRetrieverActivityStub() {
    return Workflow.newActivityStub(IngestionFlowFileRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UploadDetailsUpdateActivity buildUploadDetailsUpdateActivityStub() {
    return Workflow.newActivityStub(UploadDetailsUpdateActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
