package it.gov.pagopa.pu.migration.config;

import it.gov.pagopa.pu.migration.dto.generated.MigrationFileTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FoldersPathsConfigTest {
  private FoldersPathsConfig foldersPathsConfig;

  @BeforeEach
  void setUp() {
    foldersPathsConfig = new FoldersPathsConfig();

  }

  @Test
  void givenPopulatedPathWhenGetMigrationFilePathThenOK() {
    String expected = "/organizations";
    Map<MigrationFileTypeEnum, String> paths = new EnumMap<>(
      MigrationFileTypeEnum.class);
    paths.put(MigrationFileTypeEnum.ORGANIZATIONS, "/organizations");
    foldersPathsConfig.setMigrationFileTypePaths(paths);

    String result = foldersPathsConfig.getMigrationFilePath(
      MigrationFileTypeEnum.ORGANIZATIONS);

    Assertions.assertEquals(expected, result);
  }

  @Test
  void givenNoPathWhenGetMigrationFilePathThenUnsupportedOperation() {
    foldersPathsConfig.setMigrationFileTypePaths(new EnumMap<>(MigrationFileTypeEnum.class));
    try {
      foldersPathsConfig.getMigrationFilePath(
        MigrationFileTypeEnum.ORGANIZATIONS);
      Assertions.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      //do nothing
    }
  }
}
