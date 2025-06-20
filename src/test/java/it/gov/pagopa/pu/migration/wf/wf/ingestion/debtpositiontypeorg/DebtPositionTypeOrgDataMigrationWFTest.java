package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontypeorg;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorg.DebtPositionTypeOrgMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgDataMigrationWFTest extends BaseDataMigrationWFTest<DebtPositionTypeOrgMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new DebtPositionTypeOrgDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<DebtPositionTypeOrgMigrationFileTypeHandlerActivity>, Class<DebtPositionTypeOrgMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildDebtPositionTypeOrgMigrationFileTypeHandlerActivityStub()),
      DebtPositionTypeOrgMigrationFileTypeHandlerActivity.class
    );
  }
}
