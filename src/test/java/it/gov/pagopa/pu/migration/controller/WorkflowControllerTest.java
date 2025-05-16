package it.gov.pagopa.pu.migration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkflowService serviceMock;

  @Test
  void whenGetWorkflowStatusThenOk() throws Exception {
    String workflowId = "workflow-1";
    WorkflowStatusDTO workflowStatusDTO = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .status("ok")
      .build();

    Mockito.when(serviceMock.getWorkflowStatus(workflowId))
      .thenReturn(workflowStatusDTO);

    MvcResult result = mockMvc.perform(
        get("/migration/workflows/workflow-1/status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowStatusDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowStatusDTO.class);
    assertEquals(workflowStatusDTO, resultResponse);
  }
}
