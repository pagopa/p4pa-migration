package it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentsreporting;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentsreporting.PaymentsReportingMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class PaymentsReportDataMigrationWFTest extends BaseDataMigrationWFTest<PaymentsReportingMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new PaymentsReportingDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<PaymentsReportingMigrationFileTypeHandlerActivity>, Class<PaymentsReportingMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildPaymentsReportingMigrationFileTypeHandlerActivityStub()),
      PaymentsReportingMigrationFileTypeHandlerActivity.class
    );
  }
}
