package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.dto.FileResourceDTO;
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
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.service.wf.MigrationFileWfInvokerService;
import it.gov.pagopa.pu.migration.wf.utils.WfUtilities;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
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
  private final ZipFileService zipFileService;
  private final FileShareService fileShareService;
  private final AuthnService authnService;

  public MigrationFileServiceImpl(FileValidatorService validatorService, FoldersPathsConfig foldersPathsConfig, FileStorerService fileStorerService, UploadsRepository uploadsRepository, UploadDetailsRepository uploadDetailsRepository, MigrationFileWfInvokerService wfInvokerService, ZipFileService zipFileService, FileShareService fileShareService, AuthnService authnService) {
    this.validatorService = validatorService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.fileStorerService = fileStorerService;
    this.uploadsRepository = uploadsRepository;
    this.uploadDetailsRepository = uploadDetailsRepository;
    this.wfInvokerService = wfInvokerService;
    this.zipFileService = zipFileService;
    this.fileShareService = fileShareService;
    this.authnService = authnService;
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

    UploadDetails uploadDetail = uploadDetailsRepository.findById(uploadDetailsId).orElseThrow(() -> new EntityNotFoundException("Cannot find Upload Details having id " + uploadDetailsId));
    if(!uploadDetail.getUploadId().equals(uploadId)){
      throw new AuthorizationDeniedException("UploadDetailsId not related to requested upload");
    }
    return uploadDetail;
  }

  @Override
  public Resource getUploadsErrorsZip(String orgIpaCode, Long uploadId, UserInfo loggedUser) {
    Long organizationId = AuthorizationService.validateAdminRoleOnBroker(orgIpaCode, loggedUser).getOrganizationId();

    List<UploadDetails> uploadDetails = uploadDetailsRepository.findByUploadId(uploadId);
    if (uploadDetails.isEmpty()) {
      throw new EntityNotFoundException("Cannot find UploadDetails for uploadId " + uploadId);
    }
    List<FileResourceDTO> pdfResources = uploadDetails.stream()
      .filter(uploadDetail -> uploadDetail.getStatus().equals(IngestionFlowFileStatus.ERROR))
      .map(uploadDetail -> {
        Resource errorFile = fileShareService.downloadIngestionFlowErrorsFile(
          uploadDetail.getOrganizationId(),
          uploadDetail.getIngestionFlowFileId(),
          authnService.getAccessToken(WfUtilities.extractIpaCodeFromFileName(uploadDetail.getFileName())));
        return new FileResourceDTO(errorFile, errorFile.getFilename());
      })
      .toList();

    if (pdfResources.isEmpty()) {
      return null;
    }

    return zipFileService.zipper(pdfResources);
  }
}
