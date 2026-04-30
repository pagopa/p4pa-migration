package it.gov.pagopa.pu.migration.wf.wf.ingestion.organizations;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.organizations.OrganizationsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class OrganizationsDataMigrationWFTest extends BaseDataMigrationWFTest<OrganizationsMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new OrganizationsDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<OrganizationsMigrationFileTypeHandlerActivity>, Class<OrganizationsMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildOrganizationMigrationFileTypeHandlerActivityStub()),
      OrganizationsMigrationFileTypeHandlerActivity.class
    );
  }
}
