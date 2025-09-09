package it.gov.pagopa.pu.migration.connector.fileshare;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.migration.connector.fileshare.client.FileShareClient;
import it.gov.pagopa.pu.migration.dto.generated.ErrorDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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

  @Test
  void whenUploadIngestionFlowFileWithInvalidFileErrorThenThrowIllegalArgumentException() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);
    String errorMessage = "File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]";
    var errorDto = new ErrorDTO(
        ErrorDTO.CodeEnum.INVALID_FILE,
        errorMessage);
    HttpClientErrorException exception = Mockito.mock(HttpClientErrorException.class);
    Mockito.when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    Mockito.when(exception.getResponseBodyAs(ErrorDTO.class)).thenReturn(errorDto);
    Mockito.when(clientMock.uploadIngestionFlowFile(Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenThrow(exception);

    // When & Then
    IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () ->
      service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken)
    );
    Assertions.assertEquals(errorMessage, ex.getMessage());
  }

  @Test
  void whenUploadIngestionFlowFileWithOtherHttpClientErrorExceptionThenRethrow() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);
    String errorBody = "{\"code\":\"OTHER_ERROR\",\"message\":\"Some other error\"}";
    HttpClientErrorException exception =
        HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            errorBody.getBytes(),
            null
        );
    Mockito.when(clientMock.uploadIngestionFlowFile(Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenThrow(exception);

    // When & Then
    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class, () ->
      service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken)
    );
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    Assertions.assertTrue(ex.getResponseBodyAsString().contains("OTHER_ERROR"));
  }

  @Test
  void whenUploadIngestionFlowFileWithNonBadRequestThenRethrow() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);
    String errorBody = "{\"code\":\"SOME_ERROR\",\"message\":\"Some error\"}";
    HttpClientErrorException exception =
        HttpClientErrorException.create(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            HttpHeaders.EMPTY,
            errorBody.getBytes(),
            null
        );
    Mockito.when(clientMock.uploadIngestionFlowFile(Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenThrow(exception);

    // When & Then
    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class, () ->
      service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken)
    );
    Assertions.assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    Assertions.assertTrue(ex.getResponseBodyAsString().contains("SOME_ERROR"));
  }

  @Test
  void whenUploadIngestionFlowFileWithOtherHttpClientErrorExceptionAndNoBodyConverterThenRethrow() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);
    HttpClientErrorException exception = Mockito.mock(HttpClientErrorException.class);
    Mockito.when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    Mockito.when(exception.getResponseBodyAs(ErrorDTO.class))
      .thenThrow(new IllegalStateException("Function to convert body not set"));
    Mockito.when(clientMock.uploadIngestionFlowFile(
      Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenThrow(exception);

    // When & Then
    HttpClientErrorException ex = Assertions.assertThrows(HttpClientErrorException.class, () ->
      service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken)
    );
    Assertions.assertSame(exception, ex);
  }

  @Test
  void whenUploadIngestionFlowFileWithInvalidFileErrorAndNullMessageThenThrowIllegalArgumentExceptionWithDefault() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 0L;
    IngestionFlowFileType ingestionFlowFileType = IngestionFlowFileType.DP_INSTALLMENTS;
    Resource file = Mockito.mock(Resource.class);
    var errorDto = new ErrorDTO(
        ErrorDTO.CodeEnum.INVALID_FILE,
        null);
    HttpClientErrorException exception = Mockito.mock(HttpClientErrorException.class);
    Mockito.when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    Mockito.when(exception.getResponseBodyAs(ErrorDTO.class)).thenReturn(errorDto);
    Mockito.when(clientMock.uploadIngestionFlowFile(
      Mockito.same(organizationId), Mockito.same(ingestionFlowFileType), Mockito.same(file), Mockito.same(accessToken)))
      .thenThrow(exception);

    // When & Then
    IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () ->
      service.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, file, accessToken)
    );
    Assertions.assertEquals("File name is invalid", ex.getMessage());
  }

}
