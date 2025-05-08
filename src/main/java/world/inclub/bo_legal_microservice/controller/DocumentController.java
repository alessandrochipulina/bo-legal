package world.inclub.bo_legal_microservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.DocumentService;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.domain.request.DocumentRequest;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRepository;

@RestController
@RequestMapping("/api/v1/document")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentRepository dr;        
    @Autowired
    private DocumentService du;

    @PostMapping("/add/solicitud/{documentTypeId}")
    @Validated
    Mono<ResponseEntity<ApiResponse<Document>>> addDocumentSolicitud(
        @RequestBody @NotNull Document doc, 
        @PathVariable @NotNull Integer documentTypeId)
    {        
        log.info("addDocumentSolicitud: documentTypeId: {}", documentTypeId);
        log.info("addDocumentSolicitud: document: {}", doc.toString());
        log.info("addDocumentSolicitud: documentKey: {}", doc.getDocumentKey());

        return 
        du.addDocumentSolicitud(doc, documentTypeId)
        .map(response -> {
            ApiResponse<Document> documentApiResponse = new ApiResponse<>( response, "Solicitud de documento registrada correctamente" );
            log.debug("addDocumentSolicitud: respuesta: {}", documentApiResponse.toString());
            return ResponseEntity.ok(documentApiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede registrar")))
        .doOnError(error -> { log.error("addDocumentSolicitud: error: {}", error.getMessage(), error); });
    }

    @PostMapping("/add/rectificacion")
    @Validated
    Mono<ResponseEntity<ApiResponse<Document>>> addDocumentRectificacion(
        @RequestBody @NotNull Document doc)
    {
        return 
        du.addDocumentRectificacion(doc)
        .map(response -> {
            ApiResponse<Document> apiResponse = new ApiResponse<>( response, "Rectificación de lugar de recojo registrada correctamente");
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede registrar")));
    }

    @GetMapping("/all")
    @Validated
    public Mono<ResponseEntity<ApiResponse<List<Document>>>>
    getAllDocuments() 
    {
        return 
        dr.findAll()
        .collectList()
        .map(documents -> {
            ApiResponse<List<Document>> response = new ApiResponse<>(documents, "Documentos encontrados" );
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen documentos registrados")));
    }

    @GetMapping("/all/{documentTypeId}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<List<Document>>>>
    getAllDocumentsByType(@PathVariable @NotNull Integer documentTypeId) 
    {
        return 
        dr.findAllExtraByDocumentTypeId(documentTypeId)
        .collectList()
        .map(documents -> {
            ApiResponse<List<Document>> response = new ApiResponse<>(documents, "Documentos encontrados" );
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen documentos registrados")));
    }

    @GetMapping("/{documentKey}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<Document>>>
    getByDocumentKey(@PathVariable @NotNull String documentKey) 
    {
        log.info("getByDocumentKey: documentKey: {}", documentKey );

        return dr.findByDocumentKeyProcess(documentKey)
        .map(document -> {
            ApiResponse<Document> response = new ApiResponse<>(document, "Documento encontrado");
            log.debug("getByDocumentKey: response: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(
            dr.findByDocumentKeyVoucher(documentKey)
            .map(document -> {
                ApiResponse<Document> response = new ApiResponse<>(document, "Documento encontrado" );
                log.debug("getByDocumentKey: response: {}", response.toString());
                return ResponseEntity.ok(response);
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe")))
        ).doOnError(nullable -> log.error("getByDocumentKey: error: {}", null, nullable));
    }

    @PostMapping("/change")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>> 
    changeStatusDocument(
        @RequestBody DocumentRequest request) 
    {
        log.info("changeStatusDocument: request: {}", request.toString() );

        return 
        du.changeStatusDocument(request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>( response, "Estado del documento cambiado correctamente");
            log.debug("changeStatusDocument: respuesta: {}", apiResponse.toString());
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede rechazar")))
        .doOnError(error -> log.error("changeStatusDocument: error: {}", error.getMessage(), error));
    }

    @PostMapping("/approve")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>> 
    approveDocument(            
            @RequestBody @NotNull DocumentRequest request) 
    {
        log.info("approveDocument: request: {}", request.toString() );
        
        return 
        du.approveDocument(request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>(response,"Documento aprobado correctamente" );
            log.debug("approveDocument: respuesta: {}", apiResponse.toString());
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede aprobar")))
        .doOnError(error -> log.error("approveDocument: error: {}", error.getMessage(), error));
    }

    @PostMapping("/reject/{documentKey}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>>
    rejectDocument(
        @PathVariable @NotNull String documentKey,
        @RequestBody @NotNull DocumentRequest request) 
    {
        log.info("rejectDocument: documentKey: {}", documentKey );
        log.info("rejectDocument: request: {}", request.toString() );

        return 
        du.rejectDocument(documentKey, request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>(response, "Documento rechazado correctamente" );
            log.debug("rejectDocument: respuesta: {}", apiResponse.toString());
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede rechazar")))
        .doOnError(error -> log.error("rejectDocument: error: {}", error.getMessage(), error));
    }

    @GetMapping("/user/{userId}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<List<Document>>>>
    getAllDocumentByUserId( @PathVariable @NotNull String userId ) 
    {
        log.info("getAllDocumentByUserId: userId: {}", userId );
        
        return dr.findAllByDocumentUserId( userId )
        .collectList()
        .map(documents -> {
            ApiResponse<List<Document>> response = new ApiResponse<>(documents, "Documentos del usuario recuperados correctamente");            
            log.debug("getAllDocumentByUserId: response: {}", response );
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El usuario no tiene documentos registrados")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())))
        .doOnError(e -> log.error("getAllDocumentByUserId: error: {}", e.getMessage(), e));
    }
}
