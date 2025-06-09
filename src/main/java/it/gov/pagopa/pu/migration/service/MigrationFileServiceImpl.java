package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.exception.EntityNotFoundException;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.wf.MigrationFileWfInvokerService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MigrationFileServiceImpl implements MigrationFileService {

  private final FileValidatorService validatorService;
  private final FoldersPathsConfig foldersPathsConfig;
  private final FileStorerService fileStorerService;
  private final UploadsRepository uploadsRepository;
  private final UploadDetailsRepository uploadDetailsRepository;
  private final MigrationFileWfInvokerService wfInvokerService;

  public MigrationFileServiceImpl(FileValidatorService validatorService, FoldersPathsConfig foldersPathsConfig, FileStorerService fileStorerService, UploadsRepository uploadsRepository, UploadDetailsRepository uploadDetailsRepository, MigrationFileWfInvokerService wfInvokerService) {
    this.validatorService = validatorService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.fileStorerService = fileStorerService;
    this.uploadsRepository = uploadsRepository;
    this.uploadDetailsRepository = uploadDetailsRepository;
    this.wfInvokerService = wfInvokerService;
  }

  @Override
  public Pair<Uploads, WorkflowCreatedDTO> upload(String orgIpaCode, MigrationFileTypeEnum migrationFileType, MultipartFile migrationFile, UserInfo loggedUser) {
    Long organizationId = AuthorizationService.validateAdminRoleOnBroker(orgIpaCode, loggedUser)
       .getOrganizationId();

    validatorService.validateMultipartFile(migrationFile);

    String migrationFilePath = foldersPathsConfig.getMigrationFilePath(migrationFileType);

    String fileName = migrationFile.getOriginalFilename();
    String filePath = fileStorerService.saveToSharedFolder(organizationId, migrationFile, migrationFilePath, fileName)
      .getRelativePath();

    Uploads upload = uploadsRepository.save(Uploads.builder()
      .organizationId(organizationId)
      .fileType(migrationFileType)
      .filePathName(filePath)
      .fileName(fileName)
      .fileSize(migrationFile.getSize())
      .status(UploadsStatusEnum.UPLOADED)
      .build());

    WorkflowCreatedDTO workflowCreatedDTO = wfInvokerService.invokeWf(upload);
    return Pair.of(upload, workflowCreatedDTO);
  }

  @Override
  public List<Uploads> getUploads(String orgIpaCode, MigrationFileTypeEnum migrationFileType, UploadsStatusEnum status, UserInfo loggedUser) {
    Long organizationId = AuthorizationService.validateAdminRoleOnBroker(orgIpaCode, loggedUser)
      .getOrganizationId();

    return uploadsRepository.findByOrganizationIdAndFileTypeAndStatus(organizationId, migrationFileType, status);
  }

  @Override
  public Uploads getUpload(String orgIpaCode, Long uploadId, UserInfo loggedUser) {
    Long organizationId = AuthorizationService.validateAdminRoleOnBroker(orgIpaCode, loggedUser)
      .getOrganizationId();

    Uploads uploads = uploadsRepository.findById(uploadId).orElseThrow(() -> new EntityNotFoundException("Cannot find Upload having id " + uploadId));
    if(!uploads.getOrganizationId().equals(organizationId)){
      throw new AuthorizationDeniedException("UploadId not related to requested org");
    }
    return uploads;
  }

  @Override
  public List<UploadDetails> getUploadDetails(String orgIpaCode, Long uploadId, UserInfo loggedUser) {
    getUpload(orgIpaCode, uploadId, loggedUser);

    return uploadDetailsRepository.findByUploadId(uploadId);
  }

  @Override
  public UploadDetails getUploadDetail(String orgIpaCode, Long uploadId, Long uploadDetailsId, UserInfo loggedUser) {
    getUpload(orgIpaCode, uploadId, loggedUser);

    UploadDetails uploadDetail = uploadDetailsRepository.findById(uploadId).orElseThrow(() -> new EntityNotFoundException("Cannot find Upload having id " + uploadId));
    if(!uploadDetail.getUploadId().equals(uploadId)){
      throw new AuthorizationDeniedException("UploadDetailsId not related to requested upload");
    }
    return uploadDetail;
  }
}
