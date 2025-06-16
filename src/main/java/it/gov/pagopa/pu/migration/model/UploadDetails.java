package it.gov.pagopa.pu.migration.model;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class UploadDetails extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "upload_details_generator")
  @SequenceGenerator(name = "upload_details_generator", sequenceName = "upload_detail_id_seq", allocationSize = 1)
  private Long uploadDetailId;
  @NotNull
  private Long uploadId;
  private Long organizationId;
  private Long ingestionFlowFileId;
  @NotNull
  @Enumerated(EnumType.STRING)
  private IngestionFlowFile.IngestionFlowFileTypeEnum ingestionFlowFileType;
  private String filePathName;
  @NotNull
  private String fileName;
  @NotNull
  private Long fileSize;
  @NotNull
  @Enumerated(EnumType.STRING)
  private IngestionFlowFileStatus status;
  private String errorDescription;
  private String discardFileName;
  private Long numTotalRows;
  private Long numCorrectlyImportedRows;
}
