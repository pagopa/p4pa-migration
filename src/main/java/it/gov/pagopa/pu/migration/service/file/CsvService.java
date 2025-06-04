package it.gov.pagopa.pu.migration.service.file;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.gov.pagopa.pu.migration.wf.exception.InvalidCsvRowException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.opencsv.enums.CSVReaderNullFieldIndicator.EMPTY_SEPARATORS;

@Lazy
@Service
@Slf4j
public class CsvService {

    private final int warnThreshold;
    private final int errorThreshold;


    public CsvService(
            @Value("${export-flow-files.page-request-thresholds.warn}")int warnThreshold,
            @Value("${export-flow-files.page-request-thresholds.error}")int errorThreshold) {
        this.warnThreshold = warnThreshold;
        this.errorThreshold = errorThreshold;
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

    /**
     * Creates a CSV file from the provided supplier of beans.
     *
     * <p>This method ensures that the file is properly closed after processing
     * by using a try-with-resources statement.</p>
     *
     * <p>This method uses {@code StatefulBeanToCsv.write()}
     * to write each bean individually, reducing memory consumption at the cost of lower performance.</p>
     *
     * <p>The supplier is called repeatedly until it returns an empty list or null,
     * indicating that there are no more beans to write.</p>
     *
     * @param <C> the generic type of the beans to be written to the CSV
     * @param csvFilePath the path to the CSV file to write
     * @param typeClass the class type of the beans to be written to the CSV
     * @param csvRowsSupplier a supplier of beans to be written to the CSV (called multiple times until data are returned)
     * @param csvProfile the profile to be used for writing the CSV
     * @throws IOException if an error occurs while writing the file
     */
    public <C> void createCsv(Path csvFilePath, Class<C> typeClass, Supplier<List<C>> csvRowsSupplier, String csvProfile) throws IOException {

        File file = csvFilePath.toFile();
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
        }

        try (Writer writer = Files.newBufferedWriter(csvFilePath)) {
            HeaderColumnNameMappingStrategy<C> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
            mappingStrategy.setProfile(csvProfile);
            mappingStrategy.setType(typeClass);

            StatefulBeanToCsv<C> beanToCsv = new StatefulBeanToCsvBuilder<C>(writer)
                    .withProfile(csvProfile)
                    .withMappingStrategy(mappingStrategy)
                    .withThrowExceptions(true)
                    .build();

            List<C> rows;
            int pageRequestCount = 0;
            while (!CollectionUtils.isEmpty(rows = csvRowsSupplier.get())) {
                beanToCsv.write(rows);
                pageRequestCount++;

                if (pageRequestCount > warnThreshold && pageRequestCount <= errorThreshold) {
                    log.warn("Export process reached warn threshold page request count: {}", pageRequestCount);
                } else if (pageRequestCount > errorThreshold) {
                    throw new IllegalStateException("Export process reached error threshold page request count: " + pageRequestCount);
                }
            }

        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new InvalidCsvRowException("Invalid CSV row: " + e.getMessage());
        }
    }

    private ICSVWriter buildCsvWriter(File file) throws IOException {
        return new CSVWriterBuilder(new FileWriter(file))
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
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(EMPTY_SEPARATORS)
                    .withThrowExceptions(false)
                    .build();

            return rowProcessor.apply(csvToBean.iterator(), csvToBean.getCapturedExceptions());

        } catch (Exception e) {
            throw new IOException("Error while reading csv file: " + e.getMessage(), e);
        }
    }
}
