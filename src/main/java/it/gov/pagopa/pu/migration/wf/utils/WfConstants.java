package it.gov.pagopa.pu.migration.wf.utils;

public class WfConstants {
  private WfConstants(){}

  public static final String TASK_QUEUE_MIGRATION = "MigrationTaskQueue";

  /**
   * The Temporal Service logs a warning after 10Ki (10,240) Events and periodically logs additional warnings as new Events are added.<BR />
   * If the Event History exceeds 50Ki (51,200) Events, the Workflow Execution is terminated.
   * <BR />
   * It could be useful instead use: Workflow.getInfo().isContinueAsNewSuggested()
   */
  public static final int THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW = 50_000;
}
