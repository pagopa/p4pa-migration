package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
  private UploadDetailsRepository uploadDetailsRepositoryMock;
  @Mock
  private MigrationFileWfInvokerService wfInvokerServiceMock;
  @Mock
  private ZipFileService zipFileServiceMock;
  @Mock
  private FileShareService fileShareServiceMock;

  private MigrationFileService service;

  @BeforeEach
  void init() {
    service = new MigrationFileServiceImpl(
      validatorServiceMock,
      foldersPathsConfigMock,
      fileStorerServiceMock,
      uploadsRepositoryMock,
      uploadDetailsRepositoryMock,
      wfInvokerServiceMock,
      zipFileServiceMock,
      fileShareServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      validatorServiceMock,
      foldersPathsConfigMock,
      fileStorerServiceMock,
      uploadsRepositoryMock,
      uploadDetailsRepositoryMock,
      wfInvokerServiceMock);
  }

  private static UserInfo buildAuthorizedUser(long organizationId, String orgIpaCode) {
    return buildLoggedUser(organizationId, orgIpaCode, "ORGFC");
  }

  private static UserInfo buildUnauthorizedUser(long organizationId, String orgIpaCode) {
    return buildLoggedUser(organizationId, orgIpaCode, "ORGFC2");
  }

  private static UserInfo buildLoggedUser(long organizationId, String orgIpaCode, String orgFiscalCode) {
    UserInfo loggedUser = new UserInfo();
    loggedUser.setBrokerFiscalCode("ORGFC");
    loggedUser.setOrganizations(List.of(UserOrganizationRoles.builder()
      .operatorId("OPID")
      .organizationId(organizationId)
      .organizationIpaCode(orgIpaCode)
      .organizationFiscalCode(orgFiscalCode)
      .roles(List.of(AuthorizationService.ROLE_ADMIN))
      .build()));
    return loggedUser;
  }

//region test upload
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

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

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
//endregion

//region test getUploads
  @Test
  void givenAuthorizedUserWhenGetUploadsThenReturnIt() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    MigrationFileTypeEnum fileType = MigrationFileTypeEnum.ORGANIZATIONS;
    UploadsStatusEnum status = UploadsStatusEnum.COMPLETED;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    List<Uploads> expectedResult = List.of();
    Mockito.when(uploadsRepositoryMock.findByOrganizationIdAndFileTypeAndStatus(organizationId, fileType, status))
      .thenReturn(expectedResult);

    // Then
    List<Uploads> result = service.getUploads(orgIpaCode, fileType, status, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotAuthorizedUserWhenGetUploadsThenThrowAuthorizationDeniedException() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    MigrationFileTypeEnum fileType = MigrationFileTypeEnum.ORGANIZATIONS;
    UploadsStatusEnum status = UploadsStatusEnum.COMPLETED;

    UserInfo loggedUser = buildUnauthorizedUser(organizationId, orgIpaCode);

    // Then, When
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUploads(orgIpaCode, fileType, status, loggedUser));
  }
//endregion

//region test getUpload
  @Test
  void givenAuthorizedUserWhenGetUploadThenReturnIt() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads expectedResult = new Uploads();
    expectedResult.setOrganizationId(organizationId);

    Mockito.when(uploadsRepositoryMock.findById(uploadId))
      .thenReturn(Optional.of(expectedResult));

    // Then
    Uploads result = service.getUpload(orgIpaCode, uploadId, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotAuthorizedUserWhenGetUploadThenThrowAuthorizationDeniedException() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;

    UserInfo loggedUser = buildUnauthorizedUser(organizationId, orgIpaCode);

    // Then, When
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUpload(orgIpaCode, uploadId, loggedUser));
  }

  @Test
  void givenOrganizationIdNoRelatedWhenGetUploadThenThrowAuthorizationDeniedException() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads expectedResult = new Uploads();
    expectedResult.setOrganizationId(-1L);

    Mockito.when(uploadsRepositoryMock.findById(uploadId))
      .thenReturn(Optional.of(expectedResult));

    // Then
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUpload(orgIpaCode, uploadId, loggedUser));
  }
//endregion getUpload

//region test getUpload
  @Test
  void givenAuthorizedUserWhenGetUploadDetailsThenReturnIt() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    List<UploadDetails> expectedResult = List.of();

    service = Mockito.spy(service);
    Mockito.doReturn(null)
        .when(service)
          .getUpload(orgIpaCode, uploadId, loggedUser);

    Mockito.when(uploadDetailsRepositoryMock.findByUploadId(uploadId))
      .thenReturn(expectedResult);

    // Then
    List<UploadDetails> result = service.getUploadDetails(orgIpaCode, uploadId, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(service).getUpload(orgIpaCode, uploadId, loggedUser);
  }
//endregion getUpload

//region test getUpload
  @Test
  void givenAuthorizedUserWhenGetUploadDetailThenReturnIt() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    long uploadDetailId = 3L;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    UploadDetails expectedResult = new UploadDetails();
    expectedResult.setUploadId(uploadId);

    service = Mockito.spy(service);
    Mockito.doReturn(null)
      .when(service)
      .getUpload(orgIpaCode, uploadId, loggedUser);

    Mockito.when(uploadDetailsRepositoryMock.findById(uploadDetailId))
      .thenReturn(Optional.of(expectedResult));

    // Then
    UploadDetails result = service.getUploadDetail(orgIpaCode, uploadId, uploadDetailId, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(service).getUpload(orgIpaCode, uploadId, loggedUser);
  }

  @Test
  void givenUploadIdNoRelatedWhenGetUploadDetailThenThrowAuthorizationDeniedException() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    long uploadDetailId = 3L;

    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    UploadDetails expectedResult = new UploadDetails();
    expectedResult.setUploadId(-1L);

    service = Mockito.spy(service);
    Mockito.doReturn(null)
      .when(service)
      .getUpload(orgIpaCode, uploadId, loggedUser);

    UploadDetails wrongDetail = new UploadDetails();
    wrongDetail.setUploadId(-1L);
    Mockito.when(uploadDetailsRepositoryMock.findById(Mockito.anyLong()))
      .thenReturn(Optional.of(wrongDetail));

    // Then
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUploadDetail(orgIpaCode, uploadId, uploadDetailId, loggedUser));
  }
//endregion getUpload

//region test getUploadsErrorsZip
  @Test
  void givenUploadDetailsWithErrorsWhenGetUploadsErrorsZipThenReturnResource() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    String accessToken = "token";
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    UploadDetails errorDetail = new UploadDetails();
    errorDetail.setIngestionFlowFileId(10L);
    List<UploadDetails> uploadDetailsList = List.of(errorDetail);

    Resource resourceMock = Mockito.mock(Resource.class);
    Mockito.when(resourceMock.getFilename()).thenReturn("error.pdf");
    ByteArrayResource zipResourceMock = Mockito.mock( ByteArrayResource.class);

    Mockito.when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);
    Mockito.when(fileShareServiceMock.downloadIngestionFlowErrorsFile(organizationId, 10L, accessToken)).thenReturn(resourceMock);
    Mockito.when(zipFileServiceMock.zipper(Mockito.anyList())).thenReturn(zipResourceMock);

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser, accessToken);
    Assertions.assertSame(zipResourceMock, result);
  }

  @Test
  void givenNoUploadDetailsWhenGetUploadsErrorsZipThenThrowEntityNotFoundException() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    String accessToken = "token";
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Mockito.when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(List.of());
    Assertions.assertThrows(it.gov.pagopa.pu.migration.exception.EntityNotFoundException.class,
      () -> service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser, accessToken));
  }
//endregion
}
