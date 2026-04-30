package it.gov.pagopa.pu.migration.wf.client.ingestion;

import it.gov.pagopa.pu.migration.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.migration.wf.service.temporal.WorkflowService;
import it.gov.pagopa.pu.migration.wf.utils.TemporalTestUtils;
import it.gov.pagopa.pu.migration.wf.utils.WfConstants;
import it.gov.pagopa.pu.migration.wf.wf.ingestion.assessmentsregistry.AssessmentsRegistryDataMigrationWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryDataMigrationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private AssessmentsRegistryDataMigrationWF wfMock;

  private AssessmentsRegistryDataMigrationWFClient client;

  @BeforeEach
  void setUp() {
    client = new AssessmentsRegistryDataMigrationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenMigrateThenInvokeWfClient() {
    Long uploadId = 123L;
    String taskQueue = WfConstants.TASK_QUEUE_MIGRATION;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("AssessmentsRegistryDataMigrationWF-123", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(AssessmentsRegistryDataMigrationWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, uploadId);

    // When
    WorkflowCreatedDTO result = client.migrate(uploadId);

    // Then
    assertSame(expectedResult, result);
    verify(wfMock).migrate(uploadId);
  }
}
