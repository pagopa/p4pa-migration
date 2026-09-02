package it.gov.pagopa.pu.migration.exception.transcoder.handler;

import it.gov.pagopa.pu.migration.dto.generated.ErrorDTO;
import it.gov.pagopa.pu.migration.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.migration.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.migration.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

public class MissingServletRequestParameterExceptionMessageTranscoder implements ExceptionMessageTranscoder<MissingServletRequestParameterException> {

  @Override
  public ExceptionMessageTranscoded transcode(MissingServletRequestParameterException missingServletRequestParameterException) {
    return new ExceptionMessageTranscoded(
      ErrorDTO.CategoryEnum.BAD_REQUEST.getValue(),
      missingServletRequestParameterException.getMessage(),
      List.of(new ErrorFieldDTO(missingServletRequestParameterException.getParameterName(), "NotNull", missingServletRequestParameterException.getMessage())));
  }
}
