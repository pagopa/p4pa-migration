package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontype;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the debt position type migration data related to the input <i>uploadId</i>:
 * <p>
 * The process includes:
 * <ol>
 *   <li>Checking the existence of required files in the unzipped content</li>
 *   <li>Uploading each file using FileShareService</li>
 *   <li>Returning the {@link MigrationFileResult} with the outcome of the operation</li>
 * </ol>
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface DebtPositionTypeMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessDebtPositionTypeFile")
  MigrationFileResult processFile(Long uploadId);
}
