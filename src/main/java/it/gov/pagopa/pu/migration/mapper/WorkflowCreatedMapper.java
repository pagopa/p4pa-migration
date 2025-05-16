package it.gov.pagopa.pu.migration.mapper;

import io.temporal.api.common.v1.WorkflowExecution;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;

public class WorkflowCreatedMapper {
  private WorkflowCreatedMapper(){}

  public static WorkflowCreatedDTO map(WorkflowExecution wfExec){
    return WorkflowCreatedDTO.builder()
      .workflowId(wfExec.getWorkflowId())
      .runId(wfExec.getRunId())
      .build();
  }
}
