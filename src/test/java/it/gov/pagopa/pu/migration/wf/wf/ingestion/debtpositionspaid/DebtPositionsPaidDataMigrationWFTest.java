package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositionspaid;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositionspaid.DebtPositionsPaidMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class DebtPositionsPaidDataMigrationWFTest extends BaseDataMigrationWFTest<DebtPositionsPaidMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new DebtPositionsPaidDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<DebtPositionsPaidMigrationFileTypeHandlerActivity>, Class<DebtPositionsPaidMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildDebtPositionsPaidMigrationFileTypeHandlerActivityStub()),
      DebtPositionsPaidMigrationFileTypeHandlerActivity.class
    );
  }
}
