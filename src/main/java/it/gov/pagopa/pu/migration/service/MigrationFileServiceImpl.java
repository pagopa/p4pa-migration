package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.wf.MigrationFileWfInvokerService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MigrationFileServiceImpl implements MigrationFileService {

  private final FileValidatorService validatorService;
  private final FoldersPathsConfig foldersPathsConfig;
  private final FileStorerService fileStorerService;
  private final UploadsRepository uploadsRepository;
  private final MigrationFileWfInvokerService wfInvokerService;

  public MigrationFileServiceImpl(FileValidatorService validatorService, FoldersPathsConfig foldersPathsConfig, FileStorerService fileStorerService, UploadsRepository uploadsRepository, MigrationFileWfInvokerService wfInvokerService) {
    this.validatorService = validatorService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.fileStorerService = fileStorerService;
    this.uploadsRepository = uploadsRepository;
    this.wfInvokerService = wfInvokerService;
  }

  @Override
  public Pair<Uploads, WorkflowCreatedDTO> upload(Long organizationId, MigrationFileTypeEnum migrationFileType, MultipartFile migrationFile, UserInfo loggedUser) {
    AuthorizationService.validateAdminRoleOnBroker(organizationId, loggedUser);

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
}
