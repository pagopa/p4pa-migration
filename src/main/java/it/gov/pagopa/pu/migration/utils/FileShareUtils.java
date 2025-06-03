package it.gov.pagopa.pu.migration.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileShareUtils {

  private FileShareUtils() {
  }

  /**
   * Concatenates two path: the concatenation should not resolve into an outer folder (the
   * normalized path should still start with the first path)
   * @throws IllegalArgumentException if the described validation is not successful
   */
  public static Path concatenatePaths(String firstPath, String secondPath) {
    Path concatenatedPath = Paths.get(firstPath, secondPath).normalize();
    if (!concatenatedPath.startsWith(firstPath)) {
      log.debug("Invalid file path");
      throw new IllegalArgumentException("Invalid file path");
    }
    return concatenatedPath;
  }

  public static Path buildOrganizationBasePath(Path sharedFolder, Long organizationId) {
    return concatenatePaths(sharedFolder.toString(), String.valueOf(organizationId));
  }
}
