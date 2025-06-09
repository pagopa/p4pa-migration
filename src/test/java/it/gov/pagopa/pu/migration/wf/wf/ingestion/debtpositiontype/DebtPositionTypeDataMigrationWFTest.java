package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontype;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontype.DebtPositionTypeMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeDataMigrationWFTest extends BaseDataMigrationWFTest<DebtPositionTypeMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new DebtPositionTypeDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<DebtPositionTypeMigrationFileTypeHandlerActivity>, Class<DebtPositionTypeMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildDebtPositionTypeMigrationFileTypeHandlerActivityStub()),
      DebtPositionTypeMigrationFileTypeHandlerActivity.class
    );
  }
}
