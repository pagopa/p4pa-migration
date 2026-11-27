package it.gov.pagopa.pu.migration.wf.service.ingestion.debtposition;

import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.FileStorerService;
import it.gov.pagopa.pu.migration.wf.dto.debtposition.DebtPositionErrorDTO;
import it.gov.pagopa.pu.migration.wf.service.ingestion.ErrorArchiverService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DebtPositionErrorsArchiverService extends
  ErrorArchiverService<DebtPositionErrorDTO> {

  protected DebtPositionErrorsArchiverService(FileStorerService fileStorerService,
                                              FileArchiverService fileArchiverService,
                                              CsvService csvService) {
    super(fileStorerService, fileArchiverService, csvService);
  }

  @Override
  protected List<String[]> getHeaders() {
    return Collections.singletonList(
        new String[]{"File Name", "Ipa Code","IUPD", "Row Number", "Error Code", "Error Message"});
  }
}
