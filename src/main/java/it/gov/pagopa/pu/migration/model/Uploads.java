package it.gov.pagopa.pu.migration.model;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import it.gov.pagopa.pu.migration.enums.UploadsStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class Uploads extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uploads_generator")
  @SequenceGenerator(name = "uploads_generator", sequenceName = "upload_id_seq", allocationSize = 1)
  private Long uploadId;
  private Long organizationId;
  private String filePathName;
  private String fileName;
  private Long fileSize;
  @Enumerated(EnumType.STRING)
  private MigrationFileTypeEnum fileType;
  @Enumerated(EnumType.STRING)
  private UploadsStatusEnum status;
  private String errorDescription;
  private Integer numTotalFiles;
  private Integer numCorrectlyProcessedFiles;
}
