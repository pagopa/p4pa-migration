package it.gov.pagopa.pu.migration.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class FileShareUtilsTest {

  @Test
  void testBuildOrganizationPath() {
    Path sharedFolder = Path.of("/shared");
    Path result = FileShareUtils.buildOrganizationBasePath(sharedFolder, 1L);

    Assertions.assertEquals(Path.of("/shared/1"), result);
  }

}
