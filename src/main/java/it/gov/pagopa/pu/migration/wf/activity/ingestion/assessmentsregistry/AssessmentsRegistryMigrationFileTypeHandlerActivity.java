package it.gov.pagopa.pu.migration.wf.activity.ingestion.assessmentsregistry;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the assessments registry migration data related to the input <i>uploadId</i>:
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface AssessmentsRegistryMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessAssessmentsRegistryFile")
  MigrationFileResult processFile(Long uploadId);
}
