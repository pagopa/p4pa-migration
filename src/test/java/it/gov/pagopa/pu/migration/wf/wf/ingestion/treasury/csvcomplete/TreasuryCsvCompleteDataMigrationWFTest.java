package it.gov.pagopa.pu.migration.wf.wf.ingestion.treasury.csvcomplete;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.treasury.csvcomplete.TreasuryCsvCompleteFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class TreasuryCsvCompleteDataMigrationWFTest extends BaseDataMigrationWFTest<TreasuryCsvCompleteFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new TreasuryCsvCompleteDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<TreasuryCsvCompleteFileTypeHandlerActivity>, Class<TreasuryCsvCompleteFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildTreasuryCsvCompleteMigrationFileTypeHandlerActivityStub()),
      TreasuryCsvCompleteFileTypeHandlerActivity.class
    );
  }
}
