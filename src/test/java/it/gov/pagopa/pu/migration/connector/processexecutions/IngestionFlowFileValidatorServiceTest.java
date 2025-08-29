package it.gov.pagopa.pu.migration.connector.processexecutions;

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

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileValidatorServiceTest {

  @Mock
  private IngestionFlowFileEntityClient entityClientMock;



  private IngestionFlowFileService service;

  @BeforeEach
  void init(){
    service = new IngestionFlowFileServiceImpl(entityClientMock);
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

}
