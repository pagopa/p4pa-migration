package it.gov.pagopa.pu.migration.connector.fileshare.client;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.migration.connector.fileshare.config.FileShareApisHolder;
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
class FileShareClientTest {

  @Mock
  private FileShareApisHolder apisHolderMock;
  @Mock
  private IngestionFlowFileApi ingestionFlowFileApiMock;

  private FileShareClient client;

  @BeforeEach
  void setUp() {
    client = new FileShareClient(apisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolderMock,
      ingestionFlowFileApiMock
    );
  }

  @Test
  void whenUploadIngestionFlowFileThenInvokeApi() {
    //Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    String fileName = "FILENAME";
    Resource file = Mockito.mock(Resource.class);

    Long expectedResult = 1L;

    Mockito.when(file.getFilename())
        .thenReturn(fileName);

    Mockito.when(apisHolderMock.getIngestionFlowFileApi(accessToken))
      .thenReturn(ingestionFlowFileApiMock);
    Mockito.when(ingestionFlowFileApiMock.uploadIngestionFlowFile(
        Mockito.same(organizationId),
        Mockito.same(ingestionFlowFileType),
        Mockito.eq(FileOrigin.SIL),
        Mockito.same(file),
        Mockito.same(fileName),
        Mockito.isNull()))
      .thenReturn(UploadIngestionFlowFileResponseDTO.builder()
        .ingestionFlowFileId(expectedResult)
        .build());

    // When
    Long result = client.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenDownloadIngestionFlowErrorsFileThenInvokeApi() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 123L;
    Long ingestionFlowFileId = 456L;
    Resource expectedResource = Mockito.mock(Resource.class);

    Mockito.when(apisHolderMock.getIngestionFlowFileApi(accessToken))
      .thenReturn(ingestionFlowFileApiMock);
    Mockito.when(ingestionFlowFileApiMock.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId))
      .thenReturn(expectedResource);

    // When
    Resource result = client.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, accessToken);

    // Then
    Assertions.assertSame(expectedResource, result);
  }

}
