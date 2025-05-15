package it.gov.pagopa.pu.migration.config;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "folders")
public class FoldersPathsConfig {
  private String shared;
  private Map<MigrationFileTypeEnum,String> migrationFileTypePaths;

  public String getMigrationFilePath(MigrationFileTypeEnum ingestionFlowFileType) {
    return Optional.ofNullable(
        migrationFileTypePaths.get(ingestionFlowFileType))
      .orElseThrow(()-> {
        log.debug("No path configured for ingestionFlowFileType {}",ingestionFlowFileType);
        return new UnsupportedOperationException();
      });
  }
}
