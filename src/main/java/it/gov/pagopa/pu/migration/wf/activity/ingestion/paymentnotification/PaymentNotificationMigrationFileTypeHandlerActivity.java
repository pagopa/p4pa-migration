package it.gov.pagopa.pu.migration.wf.activity.ingestion.paymentnotification;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.migration.wf.activity.ingestion.MigrationFileTypeHandlerActivity;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;

/**
 * It will process the payment notification migration data related to the input <i>uploadId</i>:
 *
 * @see it.gov.pagopa.pu.migration.wf.activity.ingestion.BaseMigrationFileTypeHandlerActivity
 */
@ActivityInterface
public interface PaymentNotificationMigrationFileTypeHandlerActivity extends MigrationFileTypeHandlerActivity {
  @Override
  @ActivityMethod(name = "ProcessPaymentNotificationFile")
  MigrationFileResult processFile(Long uploadId);
}
