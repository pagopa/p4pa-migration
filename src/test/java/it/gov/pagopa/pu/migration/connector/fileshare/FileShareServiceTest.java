package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.client.FileShareClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class FileShareServiceTest {

  @Mock
  private FileShareClient clientMock;

  private FileShareService service;

  @BeforeEach
  void init(){
    service = new FileShareServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenUploadIngestionFlowFileThenInvokeClient(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);

    Long expectedResult = 1L;

    Mockito.when(clientMock.uploadIngestionFlowFile(Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Long result = service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenDownloadIngestionFlowErrorsFileThenInvokeClient() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 123L;
    Long ingestionFlowFileId = 456L;
    Resource expectedResource = Mockito.mock(Resource.class);

    Mockito.when(clientMock.downloadIngestionFlowErrorsFile(Mockito.same(organizationId), Mockito.same(ingestionFlowFileId), Mockito.same(accessToken)))
      .thenReturn(expectedResource);

    // When
    Resource result = service.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, accessToken);

    // Then
    Assertions.assertSame(expectedResource, result);
  }

}
