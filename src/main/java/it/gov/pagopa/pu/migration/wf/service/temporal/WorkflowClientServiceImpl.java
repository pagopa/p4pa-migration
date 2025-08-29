package it.gov.pagopa.pu.migration.wf.service.temporal;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.workflow.Functions;
import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.mapper.WorkflowCreatedMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowClientServiceImpl implements WorkflowClientService {

  public static WorkflowCreatedDTO mapAndLogWfExec(WorkflowExecution wfExec) {
    WorkflowCreatedDTO out = WorkflowCreatedMapper.map(wfExec);
    log.info("Started workflow: workflowId {}, runId {}", out.getWorkflowId(), out.getRunId());
    return out;
  }

  @Override
  public <A1> WorkflowCreatedDTO start(Functions.Proc1<A1> workflow, A1 arg1) {
    return mapAndLogWfExec(WorkflowClient.start(workflow, arg1));
  }

  @Override
  public <A1, A2> WorkflowCreatedDTO start(Functions.Proc2<A1, A2> workflow, A1 arg1, A2 arg2) {
    return mapAndLogWfExec(WorkflowClient.start(workflow, arg1, arg2));
  }

  @Override
  public <A1, A2, A3> WorkflowCreatedDTO start(Functions.Proc3<A1, A2, A3> workflow, A1 arg1, A2 arg2, A3 arg3) {
    return mapAndLogWfExec(WorkflowClient.start(workflow, arg1, arg2, arg3));
  }

  @Override
  public <A1, A2, A3, A4> WorkflowCreatedDTO start(Functions.Proc4<A1, A2, A3, A4> workflow, A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
    return mapAndLogWfExec(WorkflowClient.start(workflow, arg1, arg2, arg3, arg4));
  }

  @Override
  public <A1, A2, A3, A4, A5> WorkflowCreatedDTO start(Functions.Proc5<A1, A2, A3, A4, A5> workflow, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5) {
    return mapAndLogWfExec(WorkflowClient.start(workflow, arg1, arg2, arg3, arg4, arg5));
  }

  @Override
  public WorkflowCreatedDTO signalWithStart(WorkflowStub workflowStub, String signalName, Object[] signalArgs, Object[] startArgs){
    WorkflowExecution wfExec = workflowStub.signalWithStart(signalName, signalArgs, startArgs);
    return mapAndLogWfExec(wfExec);
  }
}
