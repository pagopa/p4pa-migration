package it.gov.pagopa.pu.migration.connector.processexecutions;

import it.gov.pagopa.pu.migration.connector.auth.AuthnService;
import it.gov.pagopa.pu.migration.connector.processexecutions.client.IngestionFlowFileEntityClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileValidatorServiceTest {

  @Mock
  private IngestionFlowFileEntityClient entityClientMock;

  @Mock
  private IngestionFlowFileEntityClient ingestionFlowFileClientMock;

  @Mock
  private AuthnService authnServiceMock;

  private IngestionFlowFileService service;
  private IngestionFlowFileService ingestionFlowFileService;

  @BeforeEach
  void init(){
    service = new IngestionFlowFileServiceImpl(entityClientMock);
    ingestionFlowFileService = new IngestionFlowFileServiceImpl(ingestionFlowFileClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      entityClientMock);
  }

  @Test
  void whenGetIngestionFlowFileThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    IngestionFlowFile expectedResult = new IngestionFlowFile();

    Mockito.when(entityClientMock.getIngestionFlowFile(Mockito.same(organizationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    IngestionFlowFile result = service.getIngestionFlowFile(organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }


  @Test
  void testFindById() {
    // Given
    String accessToken = "accessToken";
    Long ingestionFlowFileId = 1L;
    IngestionFlowFile expectedResponse = new IngestionFlowFile();
    when(ingestionFlowFileClientMock.findById(ingestionFlowFileId,accessToken)).thenReturn(expectedResponse);

    // When
    Optional<IngestionFlowFile> result = ingestionFlowFileService.findById(ingestionFlowFileId, accessToken);

    // Then
    assertTrue(result.isPresent());
    assertEquals(expectedResponse, result.get());
    verify(ingestionFlowFileClientMock, times(1)).findById(ingestionFlowFileId, accessToken);
  }


}
