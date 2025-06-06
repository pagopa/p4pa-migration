package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

public interface MigrationFileService {
  Pair<Uploads, WorkflowCreatedDTO> upload(String orgIpaCode, MigrationFileTypeEnum migrationFileType, MultipartFile migrationFile, UserInfo loggedUser);
}
