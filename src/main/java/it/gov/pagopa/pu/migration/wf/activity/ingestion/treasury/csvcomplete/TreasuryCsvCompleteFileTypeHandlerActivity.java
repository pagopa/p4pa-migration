package it.gov.pagopa.pu.migration.wf.activity.ingestion.treasury.csvcomplete;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the treasury csv complete data related to the input <i>uploadId</i>:
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface TreasuryCsvCompleteFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod
  MigrationFileResult processFile(Long uploadId);
}
