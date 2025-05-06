package world.inclub.bo_legal_microservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.DocumentUtil;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.DocumentChangeStatusRequest;
import world.inclub.bo_legal_microservice.infraestructure.repositories.CategorieRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentHistoryRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRatesRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRepository;

@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {

    @Autowired
    private DocumentRepository dr;
    @Autowired 
    private DocumentRatesRepository drr;
    @Autowired 
    private DocumentHistoryRepository dhr;
    @Autowired
    private CategorieRepository cr;
    @Autowired
    private DocumentUtil du;

    @PostMapping("/add/solicitud/{documentTypeId}")
    Mono<ResponseEntity<String>> addDocumentSolicitud(
        @RequestBody Document doc, 
        @PathVariable Integer documentTypeId)
    {        
        return du.addDocumentSolicitud(doc, documentTypeId);              
    }

    @PostMapping("/add/rectificacion")
    Mono<ResponseEntity<String>> addDocumentRectificacion(
        @RequestBody Document doc)
    {
        return du.addDocumentRectificacion(doc);
    }

    @GetMapping("/all")
    Flux<Document> getAllDocuments() 
    {
        return dr.findAll();
    }

    @GetMapping("/all/{documentTypeId}")
    Flux<Document> getAllDocumentsByType(@PathVariable Integer documentTypeId) 
    {
        return dr.findAllByDocumentTypeId(documentTypeId);
    }

    @GetMapping("/{documentKey}")
    Mono<Document> getByDocumentKey(@PathVariable String documentKey) 
    {
        return dr.findByDocumentKeyProcess(documentKey)
        .switchIfEmpty(
            dr.findByDocumentKeyVoucher(documentKey).switchIfEmpty(Mono.error(new IllegalArgumentException("Documento no encontrado")))
        );            
    }

    @PostMapping("/change")
    public Mono<ResponseEntity<String>> changeStatusDocument(
        @RequestBody DocumentChangeStatusRequest request) 
    {
        return du.changeStatusDocument(request);
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<String>> approveDocument(            
            @RequestBody DocumentChangeStatusRequest request) 
    {
        return du.approveDocument(request);        
    }

    @PostMapping("/reject/{documentKey}")
    public Mono<ResponseEntity<String>> rejectDocument(
            @PathVariable String documentKey,
            @RequestBody DocumentChangeStatusRequest request) 
    {
        return du.rejectDocument(documentKey, request);
    }

    @GetMapping("/rates/all")
    Flux<DocumentRates> getAllDocumentRates() 
    {
        return drr.findAll();
    }

    @GetMapping("/user/{userId}")
    Flux<Document> getAllDocumentByUserId(
        @PathVariable String userId
    ) 
    {
        return dr.findAllByDocumentUserId( userId )
        .switchIfEmpty(Flux.error(new IllegalArgumentException("El usuario no tiene documentos registrados")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));
    }   

    @GetMapping("/history/{documentKey}")
    Flux<DocumentHistory> getHistoryByDocumentKey(@PathVariable String documentKey) 
    {
        return dhr.findByDocumentKey( documentKey )
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existe el documento en el historial")))
        .onErrorResume(e -> Flux.error(new IllegalArgumentException(e.getMessage())));
    }

    /*
    @PostMapping("/rates/price")
    Mono<ResponseEntity<String>> setDocumentRatesPrice(
        @RequestBody DocumentRates request)    
    {
        return drr.findAll();
    }
    */

    @GetMapping("/categories/all")
    Flux<Categorie> getAllCategorie() 
    {
        return cr.findAll().switchIfEmpty(Flux.error(new IllegalArgumentException("No existen categorias registradas")));
    }

    @GetMapping("/categories/{categorieId}")
    Flux<Categorie> getCategorieById(
        @PathVariable Integer categorieId
    ) 
    {
        return cr.findAllByCategorieId( categorieId ).switchIfEmpty(Flux.error(new IllegalArgumentException("El ID de la categoria no existe")));
    }    
}
