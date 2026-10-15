package it.gov.pagopa.pu.migration.exception;

import it.gov.pagopa.pu.migration.dto.generated.ErrorDTO;
import it.gov.pagopa.pu.migration.exception.common.CommonExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler({InvalidFileException.class})
  public ResponseEntity<ErrorDTO> handleInvalidFileError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, ErrorDTO.CategoryEnum.INVALID_FILE);
  }

  @ExceptionHandler({FileUploadException.class})
  public ResponseEntity<ErrorDTO> handleFileUploadException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorDTO.CategoryEnum.FILE_UPLOAD_ERROR);
  }

  @ExceptionHandler({WorkflowNotFoundException.class})
  public ResponseEntity<ErrorDTO> handleWFNotFoundException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, ErrorDTO.CategoryEnum.NOT_FOUND);
  }

}
