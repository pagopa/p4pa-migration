package it.gov.pagopa.pu.migration.wf.service.ingestion.debtpositiontypeorgperator;

import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorErrorDTO;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.wf.service.ingestion.ErrorArchiverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Lazy
@Service
public class DebtPosTypeOrgOperatorsErrorsArchiverService extends
  ErrorArchiverService<DebtPositionTypeOrgOperatorErrorDTO> {

  protected DebtPosTypeOrgOperatorsErrorsArchiverService(@Value("${folders.shared}") String sharedFolder,
                                                         @Value("${folders.process-target-sub-folders.errors}") String errorFolder,
                                                         FileArchiverService fileArchiverService,
                                                         CsvService csvService) {
    super(sharedFolder, errorFolder, fileArchiverService, csvService);
  }

  @Override
  protected List<String[]> getHeaders() {
    return Collections.singletonList(
        new String[]{"File Name", "Ipa Code","Debt Position Type Org Code", "Row Number", "Error Code", "Error Message"});
  }
}
