package it.gov.pagopa.pu.migration.utils.faker;


import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;

import java.time.OffsetDateTime;

public class IngestionFlowFileFaker {

    public static IngestionFlowFile buildIngestionFlowFile(){
        return TestUtils.getPodamFactory().manufacturePojo(IngestionFlowFile.class)
                .ingestionFlowFileId(1L)
                .organizationId(1L)
                .status(IngestionFlowFileStatus.PROCESSING)
                .numTotalRows(3L)
                .numCorrectlyImportedRows(2L)
                .creationDate(OffsetDateTime.now())
                .updateDate(OffsetDateTime.now())
                .operatorExternalId("operatorExternalId")
                .filePathName("filePathName")
                .fileName("fileName.csv")
                .pdfGenerated(2L)
                .errorDescription("errorDescription")
                .pspIdentifier("PspId")
                .flowDateTime(OffsetDateTime.now())
                .discardFileName("DiscardFileName")
                .ingestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING)
                .fileSize(100L)
                .fileOrigin("PAGOPA")
                .fileVersion("FILEVERSION");
    }

}
