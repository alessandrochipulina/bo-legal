package world.inclub.bo_legal_microservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentHistoryRepository;

@RestController
@RequestMapping("/api/v1/document/history")
public class DocumentHistoryController {
    
    @Autowired 
    private DocumentHistoryRepository dhr;

    @GetMapping("/{documentKey}")
    Flux<DocumentHistory> getHistoryByDocumentKey(@PathVariable String documentKey) 
    {
        return dhr.findByDocumentKey( documentKey )
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existe el documento en el historial")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));
    }

}
