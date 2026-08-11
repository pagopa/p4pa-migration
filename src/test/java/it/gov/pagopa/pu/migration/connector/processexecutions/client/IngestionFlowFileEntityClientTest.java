package it.gov.pagopa.pu.migration.connector.processexecutions.client;

import it.gov.pagopa.pu.migration.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.migration.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileEntityControllerApi;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileEntityClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileControllerApi ingestionFlowFileControllerApiMock;
  @Mock
  private IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApiMock;

  private IngestionFlowFileEntityClient client;

  @BeforeEach
  void init(){
    client = new IngestionFlowFileEntityClient(processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock,
      ingestionFlowFileControllerApiMock,
      ingestionFlowFileEntityControllerApiMock
    );
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    Long ingestionFlowFileId = 123L;
    IngestionFlowFile expectedIngestionFlowFile = new IngestionFlowFile();

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenReturn(expectedIngestionFlowFile);

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertSame(expectedIngestionFlowFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long ingestionFlowFileId = 123L;

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertNull(result);
  }


  @Test
  void whenFindByIdThenOk() {
    // Given
    Long ingestionFlowFileId = 1L;
    String ingestionFlowFileIdString = String.valueOf(ingestionFlowFileId);
    IngestionFlowFile expectedResponse = new IngestionFlowFile();

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);
    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileIdString))
      .thenReturn(expectedResponse);

    // When
    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    // Then
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenNotExistentIngestionFlowFileWhenFindByIdThenNull() {
    // Given
    Long ingestionFlowFileId = 1L;
    String ingestionFlowFileIdString = String.valueOf(ingestionFlowFileId);

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);
    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileIdString))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    // Then
    assertNull(result);
  }

}
