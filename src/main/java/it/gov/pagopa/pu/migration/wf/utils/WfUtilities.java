package it.gov.pagopa.pu.migration.wf.utils;

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

}
