package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UploadsRepository extends JpaRepository<Uploads, Long> {

  @Transactional
  @Query("update Uploads" +
    " set status=:newStatus," +
    "  numTotalFiles=:#{#migrationFileResult?.numTotalFiles}," +
    "  numCorrectlyProcessedFiles=:#{#migrationFileResult?.numCorrectlyProcessedFiles}," +
    "  errorDescription=:#{#migrationFileResult?.errorDescription}" +
    " where uploadId=:uploadId" +
    "  and status=:oldStatus")
  int updateStatus(Long uploadId, UploadsStatusEnum oldStatus, UploadsStatusEnum newStatus, MigrationFileResult migrationFileResult);
}
