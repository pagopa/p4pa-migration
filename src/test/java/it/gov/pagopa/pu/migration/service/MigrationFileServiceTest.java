package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.wf.MigrationFileWfInvokerService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class MigrationFileServiceTest {

  @Mock
  private FileValidatorService validatorServiceMock;
  @Mock
  private FoldersPathsConfig foldersPathsConfigMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private UploadsRepository uploadsRepositoryMock;
  @Mock
  private MigrationFileWfInvokerService wfInvokerServiceMock;

  private MigrationFileService service;

  @BeforeEach
  void init() {
    service = new MigrationFileServiceImpl(
      validatorServiceMock,
      foldersPathsConfigMock,
      fileStorerServiceMock,
      uploadsRepositoryMock,
      wfInvokerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      validatorServiceMock,
      foldersPathsConfigMock,
      fileStorerServiceMock,
      uploadsRepositoryMock,
      wfInvokerServiceMock);
  }

  @Test
  void whenUploadThenInvokeServices() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    MigrationFileTypeEnum migrationFileType = MigrationFileTypeEnum.ORGANIZATIONS;
    MultipartFile file = Mockito.mock(MultipartFile.class);

    String migrationFileSubFolder = "pathToFile";
    String filePath = "filePath";
    String fileName = "fileName.zip";
    long fileSize = 123L;

    Mockito.when(file.getOriginalFilename())
      .thenReturn(fileName);
    Mockito.when(file.getSize())
      .thenReturn(fileSize);

    UserInfo loggedUser = new UserInfo();
    loggedUser.setBrokerFiscalCode("ORGFC");
    loggedUser.setOrganizations(List.of(UserOrganizationRoles.builder()
      .operatorId("OPID")
      .organizationId(1L)
      .organizationIpaCode(orgIpaCode)
      .organizationFiscalCode("ORGFC")
      .roles(List.of(AuthorizationService.ROLE_ADMIN))
      .build()));

    Uploads upload2Store = Uploads.builder()
      .organizationId(organizationId)
      .filePathName(filePath)
      .fileName(fileName)
      .fileSize(fileSize)
      .status(UploadsStatusEnum.UPLOADED)
      .fileType(migrationFileType)
      .build();

    Uploads storedUploads = new Uploads();
    WorkflowCreatedDTO expectedWfCreated = new WorkflowCreatedDTO();

    Mockito.when(foldersPathsConfigMock.getMigrationFilePath(migrationFileType))
      .thenReturn(migrationFileSubFolder);

    Mockito.when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, migrationFileSubFolder, fileName))
      .thenReturn(new SaveFileResultDTO(filePath, null));

    Mockito.when(uploadsRepositoryMock.save(upload2Store))
      .thenReturn(storedUploads);

    Mockito.when(wfInvokerServiceMock.invokeWf(storedUploads))
      .thenReturn(expectedWfCreated);

    // When
    Pair<Uploads, WorkflowCreatedDTO> result = service.upload(orgIpaCode, migrationFileType, file, loggedUser);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(storedUploads, result.getKey());
    Assertions.assertSame(expectedWfCreated, result.getValue());

    Mockito.verify(validatorServiceMock).validateMultipartFile(file);
  }

  @Test
  void givenNotAuthOrgWhenUploadThenInvokeServices() {
    // Given
    String orgIpaCode = "IPACODE";
    MigrationFileTypeEnum migrationFileType = MigrationFileTypeEnum.ORGANIZATIONS;
    MultipartFile file = Mockito.mock(MultipartFile.class);

    UserInfo loggedUser = new UserInfo();
    loggedUser.setBrokerFiscalCode("ORGFC");
    loggedUser.setOrganizations(List.of(UserOrganizationRoles.builder()
      .operatorId("OPID")
      .organizationId(1L)
      .organizationIpaCode(orgIpaCode)
      .organizationFiscalCode("ORGFC2")
      .roles(List.of(AuthorizationService.ROLE_ADMIN))
      .build()));

    // When
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.upload(orgIpaCode, migrationFileType, file, loggedUser));
  }
}
