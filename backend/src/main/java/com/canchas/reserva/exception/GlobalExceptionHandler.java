package com.canchas.reserva.exception;

import com.canchas.reserva.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CollisionException.class)
    public ResponseEntity<ApiErrorResponse> handleCollision(CollisionException ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.CONFLICT.value(),
            "CONFLICTO_COLISION_HORARIO",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "REGLA_DE_NEGOCIO_INVALIDA",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "RECURSO_NO_ENCONTRADO",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "ACCESO_DENEGADO",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "CREDENCIALES_INVALIDAS",
            "Correo o contraseña incorrectos.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDACION_FALLIDA",
            "Los datos enviados no cumplen con el formato requerido.",
            request.getRequestURI(),
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "ERROR_INTERNO_SERVIDOR",
            ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
