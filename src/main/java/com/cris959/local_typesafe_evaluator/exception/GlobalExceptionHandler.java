package com.cris959.local_typesafe_evaluator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura específicamente los errores HTTP devueltos por Ollama (ej. 400 Bad Request)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientException(HttpClientErrorException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getStatusCode().value(),
                "Error devuelto por Ollama",
                ex.getResponseBodyAsString() // Extrae el detalle exacto del rechazo de la API
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // Captura el error cuando Ollama está apagado o no responde en el puerto 11434
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleOllamaConnectionException(ResourceAccessException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Servicio de IA Local No Disponible",
                "No se pudo conectar con Ollama. Asegúrate de que Ollama esté encendido y ejecutándose en tu equipo."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Captura cualquier otro error general inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                "Ocurrió un error inesperado: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}