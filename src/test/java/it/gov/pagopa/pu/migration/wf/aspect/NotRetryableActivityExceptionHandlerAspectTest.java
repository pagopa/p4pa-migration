package it.gov.pagopa.pu.migration.wf.aspect;

import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivity;
import it.gov.pagopa.pu.migration.wf.activity.UploadsStatusUpdateActivityImpl;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.NotRetryableActivityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(classes = {NotRetryableActivityExceptionHandlerAspect.class, UploadsStatusUpdateActivityImpl.class})
@EnableAspectJAutoProxy
class NotRetryableActivityExceptionHandlerAspectTest {

  @MockitoSpyBean
  private UploadsStatusUpdateActivity statusActivitySpy;
  @MockitoBean
  private UploadsRepository repositoryMock;

  @Test
  void givenNotErrorWhenInvokeActivityThenReturnItsValue() {
    // Given
    long uploadId = 1L;
    UploadsStatusEnum oldStatus = UploadsStatusEnum.UPLOADED;
    UploadsStatusEnum newStatus = UploadsStatusEnum.PROCESSING;
    MigrationFileResult migrationFileResult = new MigrationFileResult();
    int expectedResult = 1;
    Mockito.when(repositoryMock.updateStatus(uploadId, oldStatus, newStatus, migrationFileResult))
      .thenReturn(expectedResult);

    // When
    int result = repositoryMock.updateStatus(uploadId, oldStatus, newStatus, migrationFileResult);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenNotRetryableActivityExceptionExtensionWhenInvokeActivityThenExceptionWrapped() {
    // Given
    long uploadId = 1L;
    UploadsStatusEnum oldStatus = UploadsStatusEnum.UPLOADED;
    UploadsStatusEnum newStatus = UploadsStatusEnum.PROCESSING;
    MigrationFileResult migrationFileResult = new MigrationFileResult();

    NotRetryableActivityException expectedNestedException = new NotRetryableActivityException("DUMMY") {};
    Mockito.when(repositoryMock.updateStatus(uploadId, oldStatus, newStatus, migrationFileResult))
      .thenThrow(expectedNestedException);

    // When
    ApplicationFailure result = Assertions.assertThrows(ApplicationFailure.class, () -> statusActivitySpy.updateStatus(uploadId, oldStatus, newStatus, migrationFileResult));

    // Then
    Assertions.assertTrue(result.isNonRetryable());
    Assertions.assertEquals(expectedNestedException.getMessage(), result.getOriginalMessage());
    Assertions.assertEquals(expectedNestedException.getClass().getName(), result.getType());
    Assertions.assertSame(expectedNestedException, result.getCause());
  }
}
