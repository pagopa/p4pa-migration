package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessmentsregistry;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessmentsregistry.AssessmentsRegistryMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryDataMigrationWFTest extends BaseDataMigrationWFTest<AssessmentsRegistryMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new AssessmentsRegistryDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<AssessmentsRegistryMigrationFileTypeHandlerActivity>, Class<AssessmentsRegistryMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildAssessmentsRegistryMigrationFileTypeHandlerActivityStub()),
      AssessmentsRegistryMigrationFileTypeHandlerActivity.class
    );
  }
}
