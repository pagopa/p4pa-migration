package it.gov.pagopa.pu.migration.service.wf;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DataMigrationWfClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.DebtPositionTypeOrgOperatorDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.OrganizationDataMigrationWFClient;
import it.gov.pagopa.pu.migration.wf.client.ingestion.PaymentsReportingDataMigrationWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MigrationFileWfInvokerServiceTest {

  @Mock
  private OrganizationDataMigrationWFClient organizationDataMigrationWFClientMock;
  @Mock
  private DebtPositionTypeOrgOperatorDataMigrationWFClient debtPositionTypeOrgOperatorDataMigrationWFClientMock;

  @Mock
  private PaymentsReportingDataMigrationWFClient paymentsReportingDataMigrationWFClientMock;

  private MigrationFileWfInvokerService service;

  private Map<MigrationFileTypeEnum, DataMigrationWfClient> fileType2ExpectedClientMock;

  @BeforeEach
  void init(){
    service = new MigrationFileWfInvokerServiceImpl(organizationDataMigrationWFClientMock,
            debtPositionTypeOrgOperatorDataMigrationWFClientMock,
            paymentsReportingDataMigrationWFClientMock
    );

    fileType2ExpectedClientMock = Map.of(
      MigrationFileTypeEnum.ORGANIZATIONS, organizationDataMigrationWFClientMock,
      MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS, debtPositionTypeOrgOperatorDataMigrationWFClientMock,
      MigrationFileTypeEnum.PAYMENTS_REPORTING, paymentsReportingDataMigrationWFClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(organizationDataMigrationWFClientMock,
            debtPositionTypeOrgOperatorDataMigrationWFClientMock,
            paymentsReportingDataMigrationWFClientMock
      );
  }

  @ParameterizedTest
  @EnumSource(MigrationFileTypeEnum.class)
  void test(MigrationFileTypeEnum fileType){
    // Given
    Uploads uploads = new Uploads();
    uploads.setFileType(fileType);

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();

    DataMigrationWfClient mock = fileType2ExpectedClientMock.get(fileType);
    Mockito.when(mock.migrate(uploads.getUploadId()))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.invokeWf(uploads);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

}
