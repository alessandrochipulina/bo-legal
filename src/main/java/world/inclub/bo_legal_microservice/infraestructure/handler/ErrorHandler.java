package world.inclub.bo_legal_microservice.infraestructure.handler;

import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.Map;


@RestControllerAdvice
@Order(-2)
public class ErrorHandler {

    // Validaciones fallidas con @Valid
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> handleValidation(WebExchangeBindException ex) {
        Map<String, String> errores = ex.getFieldErrors().stream()
            .collect(java.util.stream.Collectors.toMap(
                field -> field.getField(),
                field -> field.getDefaultMessage(),
                (v1, _) -> v1)); // evita duplicados

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            "400",
            "Error de validación",
            errores
        );
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    // Ruta no encontrada
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleNotFound(ResponseStatusException ex) {
        ApiResponse<Void> response = new ApiResponse<>(
            String.valueOf(ex.getStatusCode().value()),
            ex.getReason() != null ? ex.getReason() : "Error",
            null
        );
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(response));
    }

    // Cualquier otro error genérico
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleAll(Exception ex) {
        ApiResponse<String> response = new ApiResponse<>(
            "500",
            "Error interno del servidor",
            ex.getMessage()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}
