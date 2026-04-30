package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositions;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositions.DebtPositionsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class DebtPositionsDataMigrationWFTest extends BaseDataMigrationWFTest<DebtPositionsMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new DebtPositionsDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<DebtPositionsMigrationFileTypeHandlerActivity>, Class<DebtPositionsMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildDebtPositionsMigrationFileTypeHandlerActivityStub()),
      DebtPositionsMigrationFileTypeHandlerActivity.class
    );
  }
}
