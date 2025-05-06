package world.inclub.bo_legal_microservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRatesRepository;

@RestController
@RequestMapping("/api/v1/document/rates")
public class DocumentRatesController {
    
    @Autowired 
    private DocumentRatesRepository drr;

    @GetMapping("/all")
    Flux<DocumentRates> getAllDocumentRates() 
    {
        return drr.findAll()
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen tarifas de documentos")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));
    }
}
