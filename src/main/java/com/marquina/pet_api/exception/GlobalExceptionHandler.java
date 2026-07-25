package com.marquina.pet_api.exception;

import com.marquina.pet_api.config.CorrelationConstants;
import com.marquina.pet_api.model.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_ERROR_MESSAGE = "Ocurrió un error inesperado al procesar la solicitud";
    private static final String INVALID_FIELDS_MESSAGE = "La solicitud contiene campos inválidos";

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFound(PetNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ExternalApiTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(ExternalApiTimeoutException ex, HttpServletRequest request) {
        return build(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = "El parámetro '%s' debe ser un número válido".formatted(ex.getName());
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, INVALID_FIELDS_MESSAGE, request, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado | path={}", request.getRequestURI(), ex);

        // El mensaje de la excepcion original puede exponer detalles de
        // infraestructura, por eso al cliente solo se devuelve el generico.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE, request, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> fieldErrors) {

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(clock),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                MDC.get(CorrelationConstants.TRANSACTION_ID_MDC_KEY),
                fieldErrors);

        return ResponseEntity.status(status).body(body);
    }
}
