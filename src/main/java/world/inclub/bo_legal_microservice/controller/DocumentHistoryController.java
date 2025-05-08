package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentHistoryRepository;

@RestController
@RequestMapping("/api/v1/document/history")
@Slf4j
public class DocumentHistoryController {
    
    @Autowired 
    private DocumentHistoryRepository dhr;

    @GetMapping("/{documentKey}")
    @Validated
    Mono<ResponseEntity<ApiResponse<List<DocumentHistory>>>> 
    getHistoryByDocumentKey(@PathVariable @NotNull String documentKey) 
    {
        log.info("getHistoryByDocumentKey: documentKey: {}", documentKey);

        return dhr.findByDocumentKey( documentKey )
        .collectList()
        .map(usuario -> {
            ApiResponse<List<DocumentHistory>> response = new ApiResponse<>(usuario, "Document history retrieved successfully");
            log.debug("getHistoryByDocumentKey: response: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existe el documento en el historial")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(error -> { log.error("getHistoryByDocumentKey: error: {}", error.getMessage(), error); });
    }

}
