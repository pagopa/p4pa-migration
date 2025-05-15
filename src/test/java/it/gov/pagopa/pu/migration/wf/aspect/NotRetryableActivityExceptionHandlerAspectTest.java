package it.gov.pagopa.pu.migration.wf.aspect;

import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(classes = {NotRetryableActivityExceptionHandlerAspect.class})
@ComponentScan(basePackages = "it.gov.pagopa.payhub.activities.activity")
@EnableAspectJAutoProxy
class NotRetryableActivityExceptionHandlerAspectTest {

    @MockitoSpyBean
    private UpdateIngestionFlowStatusActivity statusActivitySpy;
    @MockitoSpyBean
    private IngestionFlowFileProcessingLockerActivity lockerActivitySpy;

    @MockitoBean
    private IngestionFlowFileService ingestionFlowFileServiceMock;

    @Test
    void givenNotErrorWhenInvokeActivityThenReturnItsValue(){
        // Given
        Mockito.when(lockerActivitySpy.acquireProcessingLock(1L))
                .thenReturn(true);

        // When
        boolean result = lockerActivitySpy.acquireProcessingLock(1L);

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    void givenNotRetryableActivityExceptionExtensionWhenInvokeActivityThenExceptionWrapped(){
        // Given
        IngestionFlowFileStatus oldStatus = IngestionFlowFileStatus.UPLOADED;
        IngestionFlowFileStatus newStatus = IngestionFlowFileStatus.PROCESSING;
        NotRetryableActivityException expectedNestedException = new NotRetryableActivityException("DUMMY"){};
        Mockito.when(ingestionFlowFileServiceMock.updateStatus(1L, oldStatus, newStatus, new IngestionFlowFileResult()))
                .thenThrow(expectedNestedException);

        // When
        ApplicationFailure result = Assertions.assertThrows(ApplicationFailure.class, () -> statusActivitySpy.updateStatus(1L, oldStatus, newStatus, new IngestionFlowFileResult()));

        // Then
        Assertions.assertTrue(result.isNonRetryable());
        Assertions.assertEquals(expectedNestedException.getMessage(), result.getOriginalMessage());
        Assertions.assertEquals(expectedNestedException.getClass().getName(), result.getType());
        Assertions.assertSame(expectedNestedException, result.getCause());
    }
}
