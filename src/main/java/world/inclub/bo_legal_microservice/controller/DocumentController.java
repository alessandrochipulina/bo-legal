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
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.DocumentService;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.domain.request.DocumentRequest;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRepository;

@RestController
@RequestMapping("/api/v1/document")
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
        return 
        du.addDocumentSolicitud(doc, documentTypeId)
        .map(response -> {
            ApiResponse<Document> apiResponse = new ApiResponse<>(
                "200",
                "Solicitud de documento registrada correctamente",
                response );
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede registrar")));
    }

    @PostMapping("/add/rectificacion")
    @Validated
    Mono<ResponseEntity<ApiResponse<Document>>> addDocumentRectificacion(
        @RequestBody @NotNull Document doc)
    {
        return 
        du.addDocumentRectificacion(doc)
        .map(response -> {
            ApiResponse<Document> apiResponse = new ApiResponse<>(
                "200",
                "Rectificación de lugar de recojo registrada correctamente",
                response );
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
            ApiResponse<List<Document>> response = new ApiResponse<>(
                "200",
                "Documentos encontrados",
                documents );
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
        dr.findAllByDocumentTypeId(documentTypeId)
        .collectList()
        .map(documents -> {
            ApiResponse<List<Document>> response = new ApiResponse<>(
                "200",
                "Documentos encontrados",
                documents );
            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/{documentKey}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<Document>>>
    getByDocumentKey(@PathVariable @NotNull String documentKey) 
    {
        return dr.findByDocumentKeyProcess(documentKey)
        .map(document -> {
            ApiResponse<Document> response = new ApiResponse<>(
                "200",
                "Documento encontrado",
                document );
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(
            dr.findByDocumentKeyVoucher(documentKey)
            .map(document -> {
                ApiResponse<Document> response = new ApiResponse<>(
                    "200",
                    "Documento encontrado",
                    document );
                return ResponseEntity.ok(response);
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe")))
        );        
    }

    @PostMapping("/change")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>> 
    changeStatusDocument(
        @RequestBody DocumentRequest request) 
    {
        return 
        du.changeStatusDocument(request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>(
                "200",
                "Estado del documento cambiado correctamente",
                response );
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede rechazar")));
    }

    @PostMapping("/approve")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>> 
    approveDocument(            
            @RequestBody @NotNull DocumentRequest request) 
    {
        return 
        du.approveDocument(request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>(
                "200",
                "Documento aprobado correctamente",
                response );
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede aprobar")));
    }

    @PostMapping("/reject/{documentKey}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<String>>>
    rejectDocument(
        @PathVariable @NotNull String documentKey,
        @RequestBody @NotNull DocumentRequest request) 
    {
        return 
        du.rejectDocument(documentKey, request)
        .map(response -> {
            ApiResponse<String> apiResponse = new ApiResponse<>(
                "200",
                "Documento rechazado correctamente",
                response );
            return ResponseEntity.ok(apiResponse);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe o no se puede rechazar")));
    }

    @GetMapping("/user/{userId}")
    @Validated
    public Mono<ResponseEntity<ApiResponse<List<Document>>>>
    getAllDocumentByUserId( @PathVariable @NotNull String userId ) 
    {
        return dr.findAllByDocumentUserId( userId )
        .collectList()
        .map(documents -> {
            ApiResponse<List<Document>> response = new ApiResponse<>(
                "200",
                "Documentos del usuario recuperados correctamente",
                documents );
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El usuario no tiene documentos registrados")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }
}
