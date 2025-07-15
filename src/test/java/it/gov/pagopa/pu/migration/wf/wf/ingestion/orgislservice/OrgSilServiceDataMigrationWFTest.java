package it.gov.pagopa.pu.migration.wf.wf.ingestion.orgislservice;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.orgsilservice.OrgSilServiceMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.orgsilservice.OrgSilServiceDataMigrationWFImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class OrgSilServiceDataMigrationWFTest extends BaseDataMigrationWFTest<OrgSilServiceMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new OrgSilServiceDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<OrgSilServiceMigrationFileTypeHandlerActivity>, Class<OrgSilServiceMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildOrgSilServiceMigrationFileTypeHandlerActivityStub()),
      OrgSilServiceMigrationFileTypeHandlerActivity.class
    );
  }
}
