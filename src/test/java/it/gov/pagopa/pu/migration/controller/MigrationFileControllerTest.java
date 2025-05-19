package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.model.Uploads;
import it.gov.pagopa.pu.migration.service.MigrationFileService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MigrationFileControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class MigrationFileControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MigrationFileService serviceMock;

  @Test
  void whenUploadThenInvokeService() throws Exception {
    long organizationId = 0L;
    MockMultipartFile file = new MockMultipartFile(
      "migrationFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Uploads upload = new Uploads();
    upload.setUploadId(1L);
    WorkflowCreatedDTO wfCreated = new WorkflowCreatedDTO();
    wfCreated.setWorkflowId("WFID");
    wfCreated.setRunId("RUNID");

    Mockito.when(serviceMock.upload(organizationId, MigrationFileTypeEnum.ORGANIZATIONS,file))
      .thenReturn(Pair.of(upload, wfCreated));

    mockMvc.perform(multipart("/migration/{migrationFileType}",MigrationFileTypeEnum.ORGANIZATIONS)
        .file(file)
        .param("organizationId", String.valueOf(organizationId))
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"uploadId\":1,\"workflowId\":\"WFID\",\"runId\":\"RUNID\"}"));
  }

}
