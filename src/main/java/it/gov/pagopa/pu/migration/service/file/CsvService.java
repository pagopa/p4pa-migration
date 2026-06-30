package it.gov.pagopa.pu.migration.service.file;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static com.opencsv.enums.CSVReaderNullFieldIndicator.EMPTY_SEPARATORS;

@Lazy
@Service
@Slf4j
public class CsvService {

    private final char separator;
    private final char quoteChar;

    public CsvService(
            @Value("${csv.separator}") char separator,
            @Value("${csv.quote-char}") char quoteChar) {
        this.separator = separator;
        this.quoteChar = quoteChar;
    }

    /**
     * Creates a CSV file from the provided header and data.
     *
     * @param csvFilePath The full path where the CSV file should be saved.
     * @param header      The header of the CSV, as a list of String[].
     * @param data        The data to write to the CSV, as a list of String[].
     * @throws IOException If an error occurs while writing the file.
     */
    public void createCsv(Path csvFilePath, List<String[]> header, List<String[]> data) throws IOException {
        // Create the destination folder if it doesn't already exist
        File file = csvFilePath.toFile();
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
        }

        // Create the CSV file
        try (ICSVWriter csvWriter = buildCsvWriter(file)) {
            // Write the header
            if (header != null && !header.isEmpty()) {
                csvWriter.writeAll(header);
            }

            // Write the data
            if (data != null && !data.isEmpty()) {
                csvWriter.writeAll(data);
            }
        }
        log.info("CSV file created successfully: {}", csvFilePath);
    }

    private ICSVWriter buildCsvWriter(File file) throws IOException {
        return new CSVWriterBuilder(new FileWriter(file))
                .withSeparator(separator)
                .withQuoteChar(quoteChar)
                .build();
    }

    /**
     * Reads a CSV file and processes it using the provided row processor.
     *
     * <p>This method ensures that the file is properly closed after processing
     * by requiring a processing function that consumes the iterator.</p>
     *
     * <p>To optimize memory usage in batch processing, this method uses {@code CsvToBean.iterator()}
     * instead of {@code parse()} or {@code stream()}. This approach reads and processes one record at a time,
     * reducing memory consumption at the cost of lower performance.</p>
     *
     * @param <T>          The generic type of the DTO to which CSV rows will be mapped.
     * @param <R>          The type of the result returned by the processing function.
     * @param csvFilePath  The path to the CSV file to read.
     * @param typeClass    The class type to map each row of the CSV to.
     * @param rowProcessor A function that processes the iterator and returns a result.
     * @return The result produced by the row processor.
     * @throws IOException If an error occurs while reading the file.
     */
    public <T, R> R readCsv(Path csvFilePath, Class<T> typeClass, BiFunction<Iterator<T>, List<CsvException>, R> rowProcessor) throws IOException {
        try (FileReader fileReader = new FileReader(csvFilePath.toFile())) {

            HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(typeClass);

            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(fileReader)
                    .withType(typeClass)
                    .withMappingStrategy(strategy)
                    .withSeparator(separator)
                    .withQuoteChar(quoteChar)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(EMPTY_SEPARATORS)
                    .withThrowExceptions(false)
                    .build();

            return rowProcessor.apply(csvToBean.iterator(), csvToBean.getCapturedExceptions());

        } catch (Exception e) {
            throw new IOException("Error while reading csv file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates and returns a StatefulBeanToCsv object ready for writing to a CSV file.
     * <p>
     * The caller is responsible for closing the provided Writer after use.
     *
     * @param <C>        the type of beans to be written
     * @param typeClass  the class of the beans
     * @param csvProfile the CSV profile to use for mapping
     * @param writer     an open Writer for the destination file
     * @return a configured StatefulBeanToCsv instance ready for writing
     */
    public <C> StatefulBeanToCsv<C> createCsvWriter(Class<C> typeClass, String csvProfile, Writer writer){
        HeaderColumnNameMappingStrategy<C> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
        mappingStrategy.setProfile(csvProfile);
        mappingStrategy.setType(typeClass);
      return new StatefulBeanToCsvBuilder<C>(writer)
              .withProfile(csvProfile)
              .withSeparator(separator)
              .withQuotechar(quoteChar)
              .withMappingStrategy(mappingStrategy)
              .withThrowExceptions(true)
              .build();
    }
}
