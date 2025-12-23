package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MigrationFileService {
  Pair<Uploads, WorkflowCreatedDTO> upload(String orgIpaCode, MigrationFileTypeEnum migrationFileType, MultipartFile migrationFile, UserInfo loggedUser);

  List<Uploads> getUploads(String orgIpaCode, MigrationFileTypeEnum migrationFileType, UploadsStatusEnum status, UserInfo loggedUser);
  Uploads getUpload(String orgIpaCode, Long uploadId, UserInfo loggedUser);
  List<UploadDetails> getUploadDetails(String orgIpaCode, Long uploadId, UserInfo loggedUser);
  UploadDetails getUploadDetail(String orgIpaCode, Long uploadId, Long uploadDetailsId, UserInfo loggedUser);
  Resource getUploadsErrorsZip(String orgIpaCode, Long uploadId, UserInfo loggedUser);

}
