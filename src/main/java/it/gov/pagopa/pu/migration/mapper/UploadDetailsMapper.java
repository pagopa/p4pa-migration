package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.model.UploadDetails;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;

public class UploadDetailsMapper {
  private UploadDetailsMapper(){}

  public static UploadDetails map(Long uploadId, IngestionFlowFile ingestionFlowFile){
    return UploadDetails.builder()
      .uploadId(uploadId)
      .ingestionFlowFileId(ingestionFlowFile.getIngestionFlowFileId())
      .ingestionFlowFileType(ingestionFlowFile.getIngestionFlowFileType())
      .filePathName(ingestionFlowFile.getFilePathName())
      .fileName(ingestionFlowFile.getFileName())
      .fileSize(ingestionFlowFile.getFileSize())
      .discardFileName(ingestionFlowFile.getDiscardFileName())
      .numCorrectlyImportedRows(ingestionFlowFile.getNumCorrectlyImportedRows())
      .numTotalRows(ingestionFlowFile.getNumTotalRows())
      .status(ingestionFlowFile.getStatus())
      .errorDescription(ingestionFlowFile.getErrorDescription())
      .build();
  }
}
