package it.gov.pagopa.pu.migration.wf.utils;

import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import org.springframework.stereotype.Component;

@Component
public class WfUtilities {

  private WfUtilities(){}

  public static String generateWorkflowId(Long id, Class<?> workflowInterface){
    return generateWorkflowId(id != null? id.toString() : null, workflowInterface);
  }

  public static String generateWorkflowId(String id, Class<?> workflowInterface) {
    if (id == null || workflowInterface == null) {
      throw new IllegalArgumentException("The ID or the workflow must not be null");
    }
    return String.format("%s-%s", workflowInterface.getSimpleName(), id);
  }

  public static String getWorkflowExceptionMessage(Exception e){
    if(e instanceof ActivityFailure activityFailure){
      if(activityFailure.getCause() instanceof ApplicationFailure applicationFailure) {
        return applicationFailure.getOriginalMessage();
      }
      return activityFailure.getMessage();
    }
    return e.getMessage();
  }

}
