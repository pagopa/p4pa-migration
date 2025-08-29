package it.gov.pagopa.pu.migration.wf.wf.ingestion.paymentnotification;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentnotification.PaymentNotificationMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationDataMigrationWFTest extends BaseDataMigrationWFTest<PaymentNotificationMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new PaymentNotificationDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<PaymentNotificationMigrationFileTypeHandlerActivity>, Class<PaymentNotificationMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildPaymentNotificationMigrationFileTypeHandlerActivityStub()),
      PaymentNotificationMigrationFileTypeHandlerActivity.class
    );
  }
}
