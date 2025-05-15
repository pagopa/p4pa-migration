package it.gov.pagopa.pu.migration.repository;

import it.gov.pagopa.pu.migration.model.Uploads;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadsRepository extends JpaRepository<Uploads, Long> {
}
