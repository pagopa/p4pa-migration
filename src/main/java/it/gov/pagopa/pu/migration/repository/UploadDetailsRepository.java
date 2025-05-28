package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UploadDetailsRepository extends JpaRepository<UploadDetails, Long> {

  @Transactional
  @Modifying
  @Query("update UploadDetails" +
    " set status=:#{#ingestionFlowFile.status}," +
    "  numTotalRows=:#{#ingestionFlowFile.numTotalRows}," +
    "  numCorrectlyImportedRows=:#{#ingestionFlowFile.numCorrectlyImportedRows}," +
    "  errorDescription=:#{#ingestionFlowFile.errorDescription}" +
    " where uploadDetailId=:uploadDetailId")
  int updateStatus(Long uploadDetailId, IngestionFlowFile ingestionFlowFile);
}
