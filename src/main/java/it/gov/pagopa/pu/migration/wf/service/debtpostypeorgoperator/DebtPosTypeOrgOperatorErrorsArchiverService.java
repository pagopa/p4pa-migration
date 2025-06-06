package it.gov.pagopa.pu.migration.wf.service.debtpostypeorgoperator;

import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorErrorDTO;
import it.gov.pagopa.pu.migration.service.file.FileArchiverService;
import it.gov.pagopa.pu.migration.service.file.CsvService;
import it.gov.pagopa.pu.migration.service.file.ErrorArchiverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Lazy
@Service
public class DebtPosTypeOrgOperatorErrorsArchiverService extends
  ErrorArchiverService<DebtPositionTypeOrgOperatorErrorDTO> {

  protected DebtPosTypeOrgOperatorErrorsArchiverService(@Value("${folders.shared}") String sharedFolder,
                                                        @Value("${folders.process-target-sub-folders.errors}") String errorFolder,
                                                        FileArchiverService fileArchiverService,
                                                        CsvService csvService) {
    super(sharedFolder, errorFolder, fileArchiverService, csvService);
  }

  @Override
  protected List<String[]> getHeaders() {
    return Collections.singletonList(
        new String[]{"File Name", "Ipa Code", "Row Number", "Error Code", "Error Message"});
  }
}
