package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.controller.generated.MigrationFileApi;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.UploadMigrationFileResponseDTO;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.security.SecurityUtils;
import it.gov.pagopa.pu.migration.service.MigrationFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class MigrationFileControllerImpl implements MigrationFileApi {

  private final MigrationFileService service;

  public MigrationFileControllerImpl(MigrationFileService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<UploadMigrationFileResponseDTO> uploadMigrationFile(MigrationFileTypeEnum migrationFileType, String orgIpaCode, MultipartFile migrationFile) {
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
}
