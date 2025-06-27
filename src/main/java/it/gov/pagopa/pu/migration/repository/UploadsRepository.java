package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UploadsRepository extends JpaRepository<Uploads, Long> {

  @Transactional
  @Modifying
  @Query("update Uploads" +
    " set status=:newStatus," +
    "  numTotalFiles=:#{#migrationFileResult?.numTotalFiles}," +
    "  numCorrectlyProcessedFiles=:#{#migrationFileResult?.numCorrectlyProcessedFiles}," +
    "  errorDescription=:#{#migrationFileResult?.errorDescription}" +
    " where uploadId=:uploadId" +
    "  and status=:oldStatus")
  int updateStatus(Long uploadId, UploadsStatusEnum oldStatus, UploadsStatusEnum newStatus, MigrationFileResult migrationFileResult);

  @Query("SELECT u " +
    " FROM Uploads u " +
    " WHERE u.organizationId=:organizationId " +
    "   AND (:fileType IS NULL OR fileType=:fileType)" +
    "   AND (:status IS NULL OR u.status=:status)" +
    " ORDER BY u.updateDate DESC")
  List<Uploads> findByOrganizationIdAndFileTypeAndStatus(Long organizationId, MigrationFileTypeEnum fileType, UploadsStatusEnum status);
}
