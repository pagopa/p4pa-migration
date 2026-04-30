package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UploadDetailsMapperTest {

  @Test
  void test() {
    // Given
    Long uploadId = 0L;
    IngestionFlowFile ingestionFlowFile = TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class);

    // When
    UploadDetails result = UploadDetailsMapper.map(uploadId, ingestionFlowFile);

    // Then
    TestUtils.checkNotNullFields(result, "uploadDetailId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertSame(uploadId, result.getUploadId());
    Assertions.assertSame(ingestionFlowFile.getIngestionFlowFileId(), result.getIngestionFlowFileId());
    Assertions.assertSame(ingestionFlowFile.getIngestionFlowFileType(), result.getIngestionFlowFileType());
    Assertions.assertSame(ingestionFlowFile.getFileName(), result.getFileName());
    Assertions.assertSame(ingestionFlowFile.getFileSize(), result.getFileSize());
    Assertions.assertSame(ingestionFlowFile.getNumCorrectlyImportedRows(), result.getNumCorrectlyImportedRows());
    Assertions.assertSame(ingestionFlowFile.getNumTotalRows(), result.getNumTotalRows());
    Assertions.assertSame(ingestionFlowFile.getErrorDescription(), result.getErrorDescription());
    Assertions.assertSame(ingestionFlowFile.getStatus(), result.getStatus());
  }
}
