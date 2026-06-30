package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.migration.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = WorkflowControllerImpl.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class WorkflowControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

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
        get("/internal/workflows/workflow-1/status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowStatusDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowStatusDTO.class);
    assertEquals(workflowStatusDTO, resultResponse);
  }
}
