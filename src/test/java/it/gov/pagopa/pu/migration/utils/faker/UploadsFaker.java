package it.gov.pagopa.pu.migration.utils.faker;


import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import it.gov.pagopa.pu.migration.model.Uploads;

public class UploadsFaker {


    public static Uploads buildUploads(MigrationFileTypeEnum fileType){
        return Uploads.builder()
          .uploadId(1L)
          .organizationId(1L)
          .filePathName("filePathName")
          .fileName("fileName.zip")
          .fileSize(100L)
          .fileType(fileType)
          .status(UploadsStatusEnum.UPLOADED)
          .build();
    }

}
