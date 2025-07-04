package it.gov.pagopa.pu.migration.wf.activity.ingestion.assessments;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the assessments migration data related to the input <i>uploadId</i>:
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface AssessmentsMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessAssessmentsFile")
  MigrationFileResult processFile(Long uploadId);
}
