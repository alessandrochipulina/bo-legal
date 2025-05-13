package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.RateService;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.domain.request.RateRequest;

@RestController
@RequestMapping("/api/v1/document/rates")
@Slf4j
public class DocumentRatesController {
    
    @Autowired 
    private RateService drr;

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

    @PostMapping("/edit")
    @Validated    
    Mono<ResponseEntity<ApiResponse<Rates>>> 
    editRate(
        @RequestBody @NotNull @Valid RateRequest rateRequest)    
    {                
        log.info("editRate: rateRequest: legalType: {}, documentType: {}, localType: {}, price: {}", 
            rateRequest.getLegalType(), rateRequest.getDocumentType(), rateRequest.getLocalType(), rateRequest.getPrice());
        
        return
        drr.update(rateRequest)
        .hasElement()
        .flatMap(existe -> {
            if (existe) {   
                log.info("setDocumentRate: Document rate exists, updating...");
                return drr.update(rateRequest)
                .map(rate -> {
                    ApiResponse<Rates> response = new ApiResponse<>(rate, "Document rate updated successfully");
                    log.debug("setDocumentRate: response: {}", response.toString());
                    return ResponseEntity.ok(response);
                });
            } 
            log.error("setDocumentRate: Document rate does not exist");
            return Mono.error(new IllegalArgumentException("No se encuentra la tarifa de documento"));            
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen tarifas de documentos")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }
}
