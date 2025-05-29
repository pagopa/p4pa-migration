package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.controller.generated.CreateOperatorDebtPositionTypeOrgApi;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.UploadMigrationFileResponseDTO;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.MigrationFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class OperatorDebtPositionTypeOrgFileControllerImpl implements CreateOperatorDebtPositionTypeOrgApi {

  private final MigrationFileService service;

  public OperatorDebtPositionTypeOrgFileControllerImpl(MigrationFileService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<UploadMigrationFileResponseDTO> uploadOperatorsDebtPositionTypeOrgFile(Long organizationId, MultipartFile migrationFile) {
    log.info("Uploading operator debt position type org file on organizationId {}: {}",
       organizationId, migrationFile.getOriginalFilename());
    Pair<Uploads, WorkflowCreatedDTO> upload = service.upload(organizationId, MigrationFileTypeEnum.OPERATOR_DEBT_POSITION_TYPE_ORG, migrationFile);
    return ResponseEntity.ok(UploadMigrationFileResponseDTO.builder()
      .uploadId(upload.getKey().getUploadId())
      .workflowId(upload.getValue().getWorkflowId())
      .runId(upload.getValue().getRunId())
      .build()
    );
  }
}
