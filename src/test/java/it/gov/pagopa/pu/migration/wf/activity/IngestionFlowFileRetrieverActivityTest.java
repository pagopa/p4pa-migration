package it.gov.pagopa.pu.migration.wf.activity;

import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.migration.wf.exception.IngestionFlowFileNotFoundException;
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
class IngestionFlowFileRetrieverActivityTest {

  @Mock
  private AuthnService authnServiceMock;
  @Mock
  private IngestionFlowFileService ingestionFlowFileServiceMock;

  private IngestionFlowFileRetrieverActivity activity;

  @BeforeEach
  void init(){
    activity = new IngestionFlowFileRetrieverActivityImpl(authnServiceMock, ingestionFlowFileServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      authnServiceMock,
      ingestionFlowFileServiceMock
    );
  }

  @Test
  void whenGetIngestionFlowFileThenInvokeService(){
    // Given
    long ingestionFlowFileId = 1L;
    String accessToken = "ACCESSTOKEN";
    IngestionFlowFile expectedResult = new IngestionFlowFile();

    Mockito.when(authnServiceMock.getAccessToken())
      .thenReturn(accessToken);
    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(expectedResult);

    // When
    IngestionFlowFile result = activity.getIngestionFlowFile(ingestionFlowFileId);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentIngestionFlowFileIdWhenGetIngestionFlowFileThenThrowIngestionFlowFileNotFoundException(){
    // Given
    long ingestionFlowFileId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(authnServiceMock.getAccessToken())
      .thenReturn(accessToken);
    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(IngestionFlowFileNotFoundException.class, () -> activity.getIngestionFlowFile(ingestionFlowFileId));
  }
}
