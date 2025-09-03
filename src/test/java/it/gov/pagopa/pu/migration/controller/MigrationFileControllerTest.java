package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.migration.security.SecurityUtilsTest;
import it.gov.pagopa.pu.migration.service.MigrationFileService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = MigrationFileControllerImpl.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class MigrationFileControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MigrationFileService serviceMock;

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void whenUploadThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";
    MockMultipartFile file = new MockMultipartFile(
      "migrationFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    MigrationFileTypeEnum fileType = MigrationFileTypeEnum.ORGANIZATIONS;
    Uploads upload = new Uploads();
    upload.setUploadId(1L);
    WorkflowCreatedDTO wfCreated = new WorkflowCreatedDTO();
    wfCreated.setWorkflowId("WFID");
    wfCreated.setRunId("RUNID");

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    Mockito.when(serviceMock.upload(Mockito.eq(orgIpaCode), Mockito.eq(fileType), Mockito.eq(file), Mockito.same(loggedUser)))
      .thenReturn(Pair.of(upload, wfCreated));

    mockMvc.perform(multipart("/migration/organization/{orgIpaCode}/{migrationFileType}",orgIpaCode, fileType)
        .file(file)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"uploadId\":1,\"workflowId\":\"WFID\",\"runId\":\"RUNID\"}"));
  }

  @Test
  void whenGetMigrationUploadsThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";
    MigrationFileTypeEnum fileType = MigrationFileTypeEnum.ORGANIZATIONS;
    UploadsStatusEnum status = UploadsStatusEnum.COMPLETED;

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    List<Uploads> expectedResult = List.of(new Uploads());
    expectedResult.getFirst().setUploadId(1L);

    Mockito.when(serviceMock.getUploads(Mockito.eq(orgIpaCode), Mockito.eq(fileType), Mockito.eq(status), Mockito.same(loggedUser)))
      .thenReturn(expectedResult);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}",orgIpaCode)
        .queryParam("status", status.toString())
        .queryParam("migrationFileType", fileType.toString())
        .contentType(MediaType.APPLICATION_JSON)
      ).andExpect(status().isOk())
      .andExpect(content().json("[{\"uploadId\":1}]"));
  }

  @Test
  void whenGetMigrationUploadThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    long uploadId = 1L;
    Uploads expectedResult = new Uploads();
    expectedResult.setUploadId(uploadId);

    Mockito.when(serviceMock.getUpload(Mockito.eq(orgIpaCode), Mockito.eq(uploadId), Mockito.same(loggedUser)))
      .thenReturn(expectedResult);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}/migrations/{uploadId}",orgIpaCode, uploadId)
        .contentType(MediaType.APPLICATION_JSON)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"uploadId\":1}"));
  }

  @Test
  void whenGetMigrationUploadDetailsThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";
    long uploadId = 0L;

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    List<UploadDetails> expectedResult = List.of(new UploadDetails());
    expectedResult.getFirst().setUploadDetailId(1L);

    Mockito.when(serviceMock.getUploadDetails(Mockito.eq(orgIpaCode), Mockito.eq(uploadId), Mockito.same(loggedUser)))
      .thenReturn(expectedResult);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}/migrations/{uploadId}/details",orgIpaCode, uploadId)
        .contentType(MediaType.APPLICATION_JSON)
      ).andExpect(status().isOk())
      .andExpect(content().json("[{\"uploadDetailId\":1}]"));
  }

  @Test
  void whenGetMigrationUploadDetailThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";
    long uploadId = 0L;

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    long uploadDetailId = 1L;
    UploadDetails expectedResult = new UploadDetails();
    expectedResult.setUploadDetailId(uploadDetailId);

    Mockito.when(serviceMock.getUploadDetail(Mockito.eq(orgIpaCode), Mockito.eq(uploadId), Mockito.eq(uploadDetailId), Mockito.same(loggedUser)))
      .thenReturn(expectedResult);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}/migrations/{uploadId}/details/{uploadDetailId}",orgIpaCode, uploadId, uploadDetailId)
        .contentType(MediaType.APPLICATION_JSON)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"uploadDetailId\":1}"));
  }


  @Test
  void whenGetMigrationErrorsThenInvokeService() throws Exception {
    String orgIpaCode = "ORGIPA";
    long uploadId = 0L;

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    byte[] fileContent = "result".getBytes();
    Resource expectedResult = new ByteArrayResource(fileContent);

    Mockito.when(serviceMock.getUploadsErrorsZip(Mockito.eq(orgIpaCode), Mockito.eq(uploadId), Mockito.same(loggedUser)))
      .thenReturn(expectedResult);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}/migrations/{uploadId}/errors",orgIpaCode, uploadId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
      ).andExpect(status().isOk())
      .andExpect(content().bytes(fileContent));
  }

  @Test
  void whenGetMigrationErrorsAndNoContentThenReturn204() throws Exception {
    String orgIpaCode = "ORGIPA";
    long uploadId = 0L;

    UserInfo loggedUser = new UserInfo();
    SecurityUtilsTest.configureSecurityContext(loggedUser);

    Mockito.when(serviceMock.getUploadsErrorsZip(Mockito.eq(orgIpaCode), Mockito.eq(uploadId), Mockito.same(loggedUser)))
      .thenReturn(null);

    mockMvc.perform(get("/migration/organization/{orgIpaCode}/migrations/{uploadId}/errors",orgIpaCode, uploadId)
        .contentType(MediaType.APPLICATION_JSON)
      ).andExpect(status().isNoContent());
  }

}
