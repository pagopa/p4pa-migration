package it.gov.pagopa.pu.migration.wf.activity;

import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.repository.UploadsRepository;
import it.gov.pagopa.pu.migration.wf.dto.MigrationFileResult;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadsStatusUpdateActivityTest {

  @Mock
  private UploadsRepository repositoryMock;

  private UploadsStatusUpdateActivity activity;

  @BeforeEach
  void init(){
    activity = new UploadsStatusUpdateActivityImpl(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void givenValidIdAndNewStatusWhenUpdateUploadStatusThenTrue(){
    // Given
    long uploadId = 1L;
    UploadsStatusEnum oldStatus = UploadsStatusEnum.UPLOADED;
    UploadsStatusEnum newStatus = UploadsStatusEnum.PROCESSING;
    MigrationFileResult migrationFileResult = new MigrationFileResult();

    Mockito.when(repositoryMock.updateStatus(Mockito.same(uploadId), Mockito.same(oldStatus), Mockito.same(newStatus), Mockito.same(migrationFileResult)))
      .thenReturn(1);

    // When, Then
    Assertions.assertDoesNotThrow(() -> activity.updateUploadStatus(uploadId, oldStatus, newStatus, migrationFileResult));
  }

  @Test
  void givenInvalidIdAndNewStatusWhenUpdateUploadStatusThenFalse(){
    // Given
    long uploadId = 1L;
    UploadsStatusEnum oldStatus = UploadsStatusEnum.UPLOADED;
    UploadsStatusEnum newStatus = UploadsStatusEnum.PROCESSING;
    MigrationFileResult migrationFileResult = new MigrationFileResult();

    Mockito.when(repositoryMock.updateStatus(Mockito.same(uploadId), Mockito.same(oldStatus), Mockito.same(newStatus), Mockito.same(migrationFileResult)))
      .thenReturn(0);

    // When, Then
    Assertions.assertThrows(UploadNotFoundException.class, () -> activity.updateUploadStatus(uploadId, oldStatus, newStatus, migrationFileResult));
  }
}
