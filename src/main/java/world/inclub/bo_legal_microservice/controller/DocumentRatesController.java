package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRatesRepository;

@RestController
@RequestMapping("/api/v1/document/rates")
@Slf4j
public class DocumentRatesController {
    
    @Autowired 
    private DocumentRatesRepository drr;

    @GetMapping("/all")
    @Validated    
    Mono<ResponseEntity<ApiResponse<List<DocumentRates>>>> 
    getAllDocumentRates() 
    {
        log.info("getAllDocumentRates: Retrieving all document rates");

        return drr.findAll()
        .collectList()
        .map(documentRates -> {
            ApiResponse<List<DocumentRates>> response = new ApiResponse<>(documentRates, "Document rates retrieved successfully");
            log.debug("getAllDocumentRates: response: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen tarifas de documentos")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }
}
