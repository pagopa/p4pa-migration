package it.gov.pagopa.pu.migration.wf.wf.ingestion.debtpositiontypeorgoperator;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorgoperator.DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;


@ExtendWith(MockitoExtension.class)
class DebtPosTypeOrgOperatorDataMigrationWFTest extends BaseDataMigrationWFTest<DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new DebtPosTypeOrgOperatorDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity>, Class<DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildDebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivityStub()),
      DebtPosTypeOrgOperatorsMigrationFileTypeHandlerActivity.class
    );
  }
}
