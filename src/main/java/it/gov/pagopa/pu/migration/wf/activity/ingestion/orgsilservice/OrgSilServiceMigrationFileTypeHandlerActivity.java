package it.gov.pagopa.pu.migration.wf.activity.ingestion.orgsilservice;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import org.springframework.core.io.Resource;

/**
 * It will process the organization migration data related to the input <i>uploadId</i>:
 * <ol>
 *   <li>TODO describe what it will be done on unzipped files, eg:</li>
 *   <li>It will check the existence of the required files</li>
 *   <li>For each, it will upload the file through {@link it.gov.pagopa.pu.migration.connector.fileshare.FileShareService#uploadIngestionFlowFile(Long, IngestionFlowFileType, Resource, String)}</li>
 *   <li>Finally it will return the {@link MigrationFileResult}</li>
 * </ol>
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface OrgSilServiceMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessOrgSilServiceFile")
  MigrationFileResult processFile(Long uploadId);
}
