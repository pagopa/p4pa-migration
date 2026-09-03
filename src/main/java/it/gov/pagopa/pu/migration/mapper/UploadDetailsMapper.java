package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;

import java.util.Objects;

public class UploadDetailsMapper {
  private UploadDetailsMapper(){}

  public static UploadDetails map(Long uploadId, IngestionFlowFile ingestionFlowFile){
    return UploadDetails.builder()
      .uploadId(uploadId)
      .ingestionFlowFileId(ingestionFlowFile.getIngestionFlowFileId())
      .ingestionFlowFileType(ingestionFlowFile.getIngestionFlowFileType())
      .organizationId(ingestionFlowFile.getOrganizationId())
      .filePathName(ingestionFlowFile.getFilePathName())
      .fileName(ingestionFlowFile.getFileName())
      .fileSize(ingestionFlowFile.getFileSize())
      .discardFileName(ingestionFlowFile.getDiscardFileName())
      .numCorrectlyImportedRows(ingestionFlowFile.getNumCorrectlyImportedRows())
      .numTotalRows(ingestionFlowFile.getNumTotalRows())
      .status(ingestionFlowFile.getStatus())
      .errorDescription(ingestionFlowFile.getErrorDescription())
      .updateOperatorExternalId(Objects.requireNonNull(
        ingestionFlowFile.getOperatorExternalId(),
        "Ingestion flow file must have an authenticated operator"))
      .build();
  }
}
