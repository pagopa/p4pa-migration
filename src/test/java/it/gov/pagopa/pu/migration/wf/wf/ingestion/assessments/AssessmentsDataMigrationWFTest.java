package it.gov.pagopa.pu.migration.wf.wf.ingestion.assessments;

import it.gov.pagopa.pu.migration.wf.activity.ingestion.assessments.AssessmentsMigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.config.stub.DataMigrationWfConfig;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFImpl;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.BaseDataMigrationWFTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class AssessmentsDataMigrationWFTest extends BaseDataMigrationWFTest<AssessmentsMigrationFileTypeHandlerActivity> {

  @Override
  protected BaseDataMigrationWFImpl buildWf() {
    return new AssessmentsDataMigrationWFImpl();
  }

  @Override
  protected Pair<OngoingStubbing<AssessmentsMigrationFileTypeHandlerActivity>, Class<AssessmentsMigrationFileTypeHandlerActivity>> getMigrationFileTypeHandlerActivityMockConfiguration(DataMigrationWfConfig configMock) {
    return Pair.of(
      Mockito.when(configMock.buildAssessmentsMigrationFileTypeHandlerActivityStub()),
      AssessmentsMigrationFileTypeHandlerActivity.class
    );
  }
}
