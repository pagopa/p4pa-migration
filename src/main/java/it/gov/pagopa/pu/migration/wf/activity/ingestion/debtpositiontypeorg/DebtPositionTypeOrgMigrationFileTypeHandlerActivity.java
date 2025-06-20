package it.gov.pagopa.pu.migration.wf.activity.ingestion.debtpositiontypeorg;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the debt position type org migration data related to the input <i>uploadId</i>:
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface DebtPositionTypeOrgMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessDebtPositionTypeOrgFile")
  MigrationFileResult processFile(Long uploadId);
}
