package it.gov.pagopa.pu.migration.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.migration.config.FoldersPathsConfig;
import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.migration.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.exception.common.NotFoundException;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.service.file.FileValidatorService;
import it.gov.pagopa.pu.migration.service.file.ZipFileService;
import it.gov.pagopa.pu.migration.service.wf.MigrationFileWfInvokerService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.MigrationFileRetrieverService;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
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

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

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
  @Mock
  private AuthnService authnService;
  @Mock
  private MigrationFileRetrieverService migrationFileRetrieverServiceMock;

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
      fileShareServiceMock,
      authnService,
      migrationFileRetrieverServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      validatorServiceMock,
      foldersPathsConfigMock,
      fileStorerServiceMock,
      uploadsRepositoryMock,
      uploadDetailsRepositoryMock,
      wfInvokerServiceMock,
      authnService);
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
      .orgSubUnitCodes(List.of())
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

    verify(validatorServiceMock).validateMultipartFile(file);
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
      .orgSubUnitCodes(List.of())
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
    when(uploadsRepositoryMock.findByOrganizationIdAndFileTypeAndStatus(organizationId, fileType, status))
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

    when(uploadsRepositoryMock.findById(uploadId))
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

    when(uploadsRepositoryMock.findById(uploadId))
      .thenReturn(Optional.of(expectedResult));

    // Then
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUpload(orgIpaCode, uploadId, loggedUser));
  }

  @Test
  void givenUploadIdNotFoundWhenGetUploadThenThrowEntityNotFoundException() {
    // Given
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.empty());

    // Then
    Assertions.assertThrows(NotFoundException.class,
      () -> service.getUpload(orgIpaCode, uploadId, loggedUser));
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

    service = spy(service);
    doReturn(null)
        .when(service)
          .getUpload(orgIpaCode, uploadId, loggedUser);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId))
      .thenReturn(expectedResult);

    // Then
    List<UploadDetails> result = service.getUploadDetails(orgIpaCode, uploadId, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
    verify(service).getUpload(orgIpaCode, uploadId, loggedUser);
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

    service = spy(service);
    doReturn(null)
      .when(service)
      .getUpload(orgIpaCode, uploadId, loggedUser);

    when(uploadDetailsRepositoryMock.findById(uploadDetailId))
      .thenReturn(Optional.of(expectedResult));

    // Then
    UploadDetails result = service.getUploadDetail(orgIpaCode, uploadId, uploadDetailId, loggedUser);

    // When
    Assertions.assertSame(expectedResult, result);
    verify(service).getUpload(orgIpaCode, uploadId, loggedUser);
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

    service = spy(service);
    doReturn(null)
      .when(service)
      .getUpload(orgIpaCode, uploadId, loggedUser);

    UploadDetails wrongDetail = new UploadDetails();
    wrongDetail.setUploadId(-1L);
    when(uploadDetailsRepositoryMock.findById(Mockito.anyLong()))
      .thenReturn(Optional.of(wrongDetail));

    // Then
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.getUploadDetail(orgIpaCode, uploadId, uploadDetailId, loggedUser));
  }
//endregion getUpload

//region test getUploadsErrorsZip
  @Test
  void givenDebtPositionTypeOrgOperatorsErrorZipWhenGetUploadsErrorsZipThenReturnDecryptedResource() throws Exception {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    String filePathName = "migration-data/debt-positions-type-org-operators";
    String fileName = "operators.csv";
    String errorZipFileName = "ERROR-operators.zip";
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = Uploads.builder()
      .uploadId(uploadId)
      .organizationId(organizationId)
      .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS)
      .filePathName(filePathName)
      .fileName(fileName)
      .build();
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));
    when(migrationFileRetrieverServiceMock.retrieveFile(organizationId, Path.of(filePathName), errorZipFileName))
      .thenReturn(new ByteArrayInputStream("zip-content".getBytes()));

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);

    Assertions.assertEquals(errorZipFileName, result.getFilename());
    try (var inputStream = result.getInputStream()) {
      Assertions.assertArrayEquals("zip-content".getBytes(), inputStream.readAllBytes());
    }
    verify(uploadDetailsRepositoryMock, never()).findByUploadId(Mockito.anyLong());
  }

  @Test
  void givenDebtPositionTypeOrgOperatorsWithoutErrorZipWhenGetUploadsErrorsZipThenReturnNull() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = Uploads.builder()
      .uploadId(uploadId)
      .organizationId(organizationId)
      .fileType(MigrationFileTypeEnum.DEBT_POSITIONS_TYPE_ORG_OPERATORS)
      .filePathName("migration-data/debt-positions-type-org-operators")
      .fileName("operators.csv")
      .build();
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);

    Assertions.assertNull(result);
    Assertions.assertTrue(Mockito.mockingDetails(migrationFileRetrieverServiceMock).getInvocations().stream()
      .anyMatch(invocation -> invocation.getMethod().getName().equals("retrieveFile")));
    verify(uploadDetailsRepositoryMock, never()).findByUploadId(Mockito.anyLong());
  }

  @Test
  void givenUploadDetailsWithErrorsWhenGetUploadsErrorsZipThenReturnResource() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    UploadDetails errorDetail = new UploadDetails();
    errorDetail.setIngestionFlowFileId(10L);
    errorDetail.setFileName("ipa1-error.csv");
    errorDetail.setStatus(IngestionFlowFileStatus.ERROR);
    List<UploadDetails> uploadDetailsList = List.of(errorDetail);

    Resource resourceMock = mock(Resource.class);
    when(resourceMock.getFilename()).thenReturn("error.pdf");
    ByteArrayResource zipResourceMock = mock( ByteArrayResource.class);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);
    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
        Mockito.any(),
        Mockito.any(),
        Mockito.any()
    )).thenReturn(resourceMock);
    when(zipFileServiceMock.zipper(Mockito.anyList())).thenReturn(zipResourceMock);
    when(authnService.getAccessToken(Mockito.anyString())).thenReturn("token");

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);
    Assertions.assertSame(zipResourceMock, result);
  }

  @Test
  void givenUploadDetailsWithWarningWhenGetUploadsErrorsZipThenReturnResource() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    UploadDetails errorDetail = new UploadDetails();
    errorDetail.setIngestionFlowFileId(10L);
    errorDetail.setFileName("ipa1-error.csv");
    errorDetail.setStatus(IngestionFlowFileStatus.WARNING);
    List<UploadDetails> uploadDetailsList = List.of(errorDetail);

    Resource resourceMock = mock(Resource.class);
    when(resourceMock.getFilename()).thenReturn("error.pdf");
    ByteArrayResource zipResourceMock = mock( ByteArrayResource.class);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);
    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
      Mockito.any(),
      Mockito.any(),
      Mockito.any()
    )).thenReturn(resourceMock);
    when(zipFileServiceMock.zipper(Mockito.anyList())).thenReturn(zipResourceMock);
    when(authnService.getAccessToken(Mockito.anyString())).thenReturn("token");

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);
    Assertions.assertSame(zipResourceMock, result);
  }

  @Test
  void givenUploadDetailsWithWarningButFileNotExistsWhenGetUploadsErrorsZipThenReturnNull() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    UploadDetails warningDetail = new UploadDetails();
    warningDetail.setIngestionFlowFileId(10L);
    warningDetail.setFileName("ipa1-warning.csv");
    warningDetail.setStatus(IngestionFlowFileStatus.WARNING);
    List<UploadDetails> uploadDetailsList = List.of(warningDetail);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);
    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
      Mockito.any(),
      Mockito.any(),
      Mockito.any()
    )).thenReturn(null);
    when(authnService.getAccessToken(Mockito.anyString())).thenReturn("token");

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);
    Assertions.assertNull(result);
    verify(zipFileServiceMock, never()).zipper(Mockito.anyList());
  }


  @Test
  void givenNoUploadDetailsWhenGetUploadsErrorsZipThenThrowEntityNotFoundException() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(List.of());
    Assertions.assertThrows(NotFoundException.class,
      () -> service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser));
  }

  @Test
  void givenUploadDetailsWithErrorsButFileNotExistsWhenGetUploadsErrorsZipThenReturnNull() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    UploadDetails errorDetail = new UploadDetails();
    errorDetail.setIngestionFlowFileId(10L);
    errorDetail.setFileName("ipa1-error.csv");
    errorDetail.setStatus(IngestionFlowFileStatus.ERROR);
    List<UploadDetails> uploadDetailsList = List.of(errorDetail);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);
    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
        Mockito.any(),
        Mockito.any(),
        Mockito.any()
    )).thenReturn(null);
    when(authnService.getAccessToken(Mockito.anyString())).thenReturn("token");

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);
    Assertions.assertNull(result);
    verify(zipFileServiceMock, never()).zipper(Mockito.anyList());
  }

  @Test
  void givenDownloadErrorsFileThrowsExceptionWhenGetUploadsErrorsZipThenSkipFileAndContinue() {
    long organizationId = 1L;
    String orgIpaCode = "IPACODE";
    long uploadId = 2L;
    UserInfo loggedUser = buildAuthorizedUser(organizationId, orgIpaCode);

    Uploads uploads = new Uploads();
    uploads.setOrganizationId(organizationId);
    when(uploadsRepositoryMock.findById(uploadId)).thenReturn(Optional.of(uploads));

    UploadDetails errorDetail1 = new UploadDetails();
    errorDetail1.setIngestionFlowFileId(10L);
    errorDetail1.setFileName("ipa1-error1.csv");
    errorDetail1.setOrganizationId(organizationId);
    errorDetail1.setStatus(IngestionFlowFileStatus.ERROR);

    UploadDetails errorDetail2 = new UploadDetails();
    errorDetail2.setIngestionFlowFileId(11L);
    errorDetail2.setFileName("ipa1-error2.csv");
    errorDetail2.setOrganizationId(organizationId);
    errorDetail2.setStatus(IngestionFlowFileStatus.ERROR);

    List<UploadDetails> uploadDetailsList = List.of(errorDetail1, errorDetail2);

    Resource resourceMock = mock(Resource.class);
    when(resourceMock.getFilename()).thenReturn("error2.pdf");
    ByteArrayResource zipResourceMock = mock(ByteArrayResource.class);

    when(uploadDetailsRepositoryMock.findByUploadId(uploadId)).thenReturn(uploadDetailsList);

    // First call throws exception, second call succeeds
    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
        Mockito.eq(organizationId),
        Mockito.eq(10L),
        Mockito.anyString()
    )).thenThrow(new RuntimeException("File not found on server"));

    when(fileShareServiceMock.downloadIngestionFlowErrorsFile(
        Mockito.eq(organizationId),
        Mockito.eq(11L),
        Mockito.anyString()
    )).thenReturn(resourceMock);

    when(zipFileServiceMock.zipper(Mockito.anyList())).thenReturn(zipResourceMock);
    when(authnService.getAccessToken(Mockito.anyString())).thenReturn("token");

    Resource result = service.getUploadsErrorsZip(orgIpaCode, uploadId, loggedUser);

    Assertions.assertSame(zipResourceMock, result);
    // Verify that zipper was called with only one file (the successful one)
    verify(zipFileServiceMock).zipper(Mockito.argThat(list -> list.size() == 1));
  }
//endregion
}
