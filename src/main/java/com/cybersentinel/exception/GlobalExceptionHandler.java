package com.cybersentinel.exception;

import com.cybersentinel.dto.common.ApiError;
import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.common.FieldViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every exception raised inside Spring MVC into the locked error envelope.
 * Extending ResponseEntityExceptionHandler routes all framework exceptions (400/404/405/406/415...)
 * through handleExceptionInternal, so none of them escape with Spring's default error shape.
 * Raw exception text is never sent to the client for unexpected errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- our own exceptions -------------------------------------------------

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex) {
        ErrorCode code = ex.getErrorCode();
        return build(code.getHttpStatus(), new HttpHeaders(), code, ex.getMessage(), ex.getDetails());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldViolation> details = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_ERROR.getHttpStatus(), new HttpHeaders(),
                ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    /** Last resort. Logs the full stack trace, returns a generic message. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return build(code.getHttpStatus(), new HttpHeaders(), code, code.getDefaultMessage(), null);
    }

    // ---- framework exceptions with useful details ---------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(status, headers, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> details = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldViolation(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return build(status, headers, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        List<FieldViolation> details = List.of(new FieldViolation(ex.getParameterName(), "is required"));
        return build(status, headers, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        List<FieldViolation> details = List.of(new FieldViolation(field, "has an invalid value"));
        return build(status, headers, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(status, headers, ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(), null);
    }

    /** Catch-all for every other Spring MVC exception (404, 405, 406, 415, malformed body...). */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ErrorCode code = ErrorCode.fromStatus(statusCode);
        return build(statusCode, headers, code, code.getDefaultMessage(), null);
    }

    // ---- helper -------------------------------------------------------------

    private ResponseEntity<Object> build(
            HttpStatusCode status, HttpHeaders headers, ErrorCode code, String message, Object details) {
        ApiResponse<Void> payload = ApiResponse.failure(new ApiError(code.name(), message, details));
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload);
    }
}
