package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.controller.generated.MigrationFileApi;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.UploadMigrationFileResponseDTO;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.security.SecurityUtils;
import it.gov.pagopa.pu.migration.service.MigrationFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
public class MigrationFileControllerImpl implements MigrationFileApi {

  private final MigrationFileService service;

  public MigrationFileControllerImpl(MigrationFileService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<UploadMigrationFileResponseDTO> uploadMigrationFile(String orgIpaCode, MigrationFileTypeEnum migrationFileType, MultipartFile migrationFile) {
    log.info("Uploading migration file type {} on organization ipa code {}: {}",
      migrationFileType, orgIpaCode, migrationFile.getOriginalFilename());
    Pair<Uploads, WorkflowCreatedDTO> upload = service.upload(orgIpaCode, migrationFileType, migrationFile, SecurityUtils.getLoggedUser());
    return ResponseEntity.ok(UploadMigrationFileResponseDTO.builder()
      .uploadId(upload.getKey().getUploadId())
      .workflowId(upload.getValue().getWorkflowId())
      .runId(upload.getValue().getRunId())
      .build()
    );
  }

  @Override
  public ResponseEntity<List<Uploads>> getMigrationUploads(String orgIpaCode, MigrationFileTypeEnum migrationFileType, UploadsStatusEnum status) {
    log.info("Requesting uploads of type {} from org {}", migrationFileType, orgIpaCode);
    return ResponseEntity.ok(service.getUploads(orgIpaCode, migrationFileType, status, SecurityUtils.getLoggedUser()));
  }

  @Override
  public ResponseEntity<Uploads> getMigrationUpload(String orgIpaCode, Long uploadId) {
    log.info("Requesting upload {} from org {}", uploadId, orgIpaCode);
    return ResponseEntity.ok(service.getUpload(orgIpaCode, uploadId, SecurityUtils.getLoggedUser()));
  }

  @Override
  public ResponseEntity<List<UploadDetails>> getMigrationUploadDetails(String orgIpaCode, Long uploadId) {
    log.info("Requesting upload details of {} from org {}", uploadId, orgIpaCode);
    return ResponseEntity.ok(service.getUploadDetails(orgIpaCode, uploadId, SecurityUtils.getLoggedUser()));
  }

  @Override
  public ResponseEntity<UploadDetails> getMigrationUploadDetail(String orgIpaCode, Long uploadId, Long uploadDetailsId) {
    log.info("Requesting upload detail {} of {} from org {}", uploadDetailsId, uploadId, orgIpaCode);
    return ResponseEntity.ok(service.getUploadDetail(orgIpaCode, uploadId, uploadDetailsId, SecurityUtils.getLoggedUser()));
  }
}
