package it.gov.pagopa.pu.migration.service.file;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.exceptions.CsvException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvServiceTest {

  private final CsvService csvService = new CsvService(';','\"');

  @Test
  void testCreateCsv_success() throws IOException {
    // Give
    Path filePath = Path.of("build", "tmp", "test", "output.csv");

    String[] headerArray = new String[]{"Header1", "Header2"};
    List<String[]> header = new ArrayList<>(List.of());
    header.add(headerArray);
    List<String[]> data = Arrays.asList(new String[]{"Data1", "Data2"}, new String[]{"Data3", "Data4"});

    // When
    csvService.createCsv(filePath, header, data);

    // Then
    File file = filePath.toFile();
    assertTrue(file.exists(), "The file should exist.");
    assertTrue(file.length() > 0, "The file should not be empty.");
  }

  @Test
  void testCreateCsv_noData() throws IOException {
    // Give
    Path filePath = Path.of("build", "tmp", "test", "empty.csv");
    String[] headerArray = new String[]{"Header1", "Header2"};
    List<String[]> header = new ArrayList<>(List.of());
    header.add(headerArray);
    List<String[]> data = List.of();

    // When
    csvService.createCsv(filePath, header, data);

    // Then
    File file = filePath.toFile();
    assertTrue(file.exists(), "The file should exist.");
    assertTrue(file.length() > 0, "The file should not be empty.");
  }

  @Test
  void testCreateCsv_noHeader() throws IOException {
    // Give
    Path filePath = Path.of("build", "tmp", "test", "no_header.csv");
    List<String[]> header = List.of();
    List<String[]> data = Arrays.asList(new String[]{"Data1", "Data2"}, new String[]{"Data3", "Data4"});

    // When
    csvService.createCsv(filePath, header, data);

    // Then
    File file = filePath.toFile();
    assertTrue(file.exists(), "The file should exist.");
    assertTrue(file.length() > 0, "The file should not be empty.");
  }

  @Test
  void testReadCsv_success() throws IOException {
    // Given
    Path filePath = Path.of("build", "tmp", "test", "input.csv");
    String[] row1 = {"Data1", "Data2", "2025-02-20"};
    String[] row2 = {"Data4", "Data5", "2025-02-20"};
    List<String[]> data = Arrays.asList(
      row1,
      new String[]{"Data2", "Data5", "WRONGDATA"},
      row2
    );
    List<String> headers = List.of("Column1", "Column2", "Column3");
    List<String[]> headerList = new ArrayList<>();
    headerList.add(headers.toArray(new String[0]));

    csvService.createCsv(filePath, headerList, data);
    List<CsvException> totalReaderExceptions = new ArrayList<>();

    // When
    List<TestCsv> resultList = csvService.readCsv(filePath, TestCsv.class, (iterator, readerException) -> {
      List<TestCsv> list = new ArrayList<>();
      iterator.forEachRemaining(list::add);
      totalReaderExceptions.clear();
      totalReaderExceptions.addAll(readerException);
      return list;
    });

    // Then
    List<String[]> actualData = resultList.stream()
      .map(testCsv -> new String[]{
        testCsv.getColumn1(),
        testCsv.getColumn2(),
        String.valueOf(testCsv.getColumn3())
      })
      .toList();

    assertEquals(2, resultList.size());
    assertEquals(2, actualData.size());
    assertArrayEquals(new String[][]{row1, row2}, actualData.toArray(new String[0][]));
    assertEquals(1, totalReaderExceptions.size());
    assertEquals("Text 'WRONGDATA' could not be parsed at index 0", totalReaderExceptions.getFirst().getCause().getMessage());
  }

  @Test
  void testReadCsv_emptyFile() throws IOException {
    // Given
    Path filePath = Path.of("build", "tmp", "test", "empty.csv");
    List<String> headers = List.of("Column1", "Column2", "Column3");
    List<String[]> headerList = new ArrayList<>();
    headerList.add(headers.toArray(new String[0]));
    List<String[]> data = List.of();

    csvService.createCsv(filePath, headerList, data);

    // When
    List<TestCsv> resultList = csvService.readCsv(filePath, TestCsv.class, (iterator, readerExceptions) -> {
      List<TestCsv> list = new ArrayList<>();
      iterator.forEachRemaining(list::add);
      return list;
    });

    // Then
    assertEquals(0, resultList.size());
  }

  @Test
  void testReadCsv_requiredColumn() throws IOException {
    // Given
    Path filePath = Path.of("build", "tmp", "test", "empty.csv");
    List<String> headers = List.of("Column1", "Column3");
    List<String[]> headerList = new ArrayList<>();
    headerList.add(headers.toArray(new String[0]));
    List<String[]> data = List.of();

    csvService.createCsv(filePath, headerList, data);

    // When & Then
    assertThrows(IOException.class, () ->
      csvService.readCsv(filePath, TestCsv.class, (iterator, readerExceptions) -> {
        List<TestCsv> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
      })
    );
  }


  @Test
  void testReadCsv_invalidFile() {
    // Given
    Path filePath = Path.of("build", "tmp", "test", "nonexistent.csv");

    // When & Then
    assertThrows(IOException.class, () ->
      csvService.readCsv(filePath, TestCsv.class, (iterator, readerExceptions) -> {
        List<TestCsv> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
      })
    );
  }

  @Test
  void testCreateCsvWriter_returnsConfiguredBeanToCsv() throws Exception {
    StringWriter writer = new StringWriter();
    StatefulBeanToCsv<TestBean> beanToCsv = csvService.createCsvWriter(TestBean.class, "default", writer);
    assertNotNull(beanToCsv);
    TestBean bean = new TestBean();
    bean.setField("value");
    beanToCsv.write(List.of(bean));
    writer.flush();
    String csvContent = writer.toString();
    assertTrue(csvContent.contains("value"));
  }

  @Setter
  @Getter
  public static class TestBean {

    @com.opencsv.bean.CsvBindByName
    private String field;

  }
}
