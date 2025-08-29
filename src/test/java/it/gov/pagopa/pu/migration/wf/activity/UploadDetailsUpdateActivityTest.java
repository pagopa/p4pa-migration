package it.gov.pagopa.pu.migration.wf.activity;

import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.repository.UploadDetailsRepository;
import it.gov.pagopa.pu.migration.wf.exception.UploadNotFoundException;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadDetailsUpdateActivityTest {

  @Mock
  private UploadDetailsRepository repositoryMock;

  private UploadDetailsUpdateActivity activity;

  @BeforeEach
  void init(){
    activity = new UploadDetailsUpdateActivityImpl(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void whenSaveDetailThenInvokeRepository(){
    // Given
    UploadDetails newEntity = new UploadDetails();
    UploadDetails storedEntity = new UploadDetails();

    Mockito.when(repositoryMock.save(Mockito.same(newEntity)))
      .thenReturn(storedEntity);

    // When
    UploadDetails result = activity.saveDetail(newEntity);

    // Then
    Assertions.assertSame(storedEntity, result);
  }

  @Test
  void givenValidIdAndNewStatusWhenUpdateDetailStatusThenTrue(){
    // Given
    long uploadDetailId = 1L;
    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();

    Mockito.when(repositoryMock.updateStatus(Mockito.same(uploadDetailId), Mockito.same(ingestionFlowFile)))
      .thenReturn(1);

    // When, Then
    Assertions.assertDoesNotThrow(() -> activity.updateDetailStatus(uploadDetailId, ingestionFlowFile));
  }

  @Test
  void givenInvalidIdAndNewStatusWhenUpdateDetailStatusThenFalse(){
    // Given
    long uploadDetailId = 1L;
    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();

    Mockito.when(repositoryMock.updateStatus(Mockito.same(uploadDetailId), Mockito.same(ingestionFlowFile)))
      .thenReturn(0);

    // When, Then
    Assertions.assertThrows(UploadNotFoundException.class, () -> activity.updateDetailStatus(uploadDetailId, ingestionFlowFile));
  }
}
