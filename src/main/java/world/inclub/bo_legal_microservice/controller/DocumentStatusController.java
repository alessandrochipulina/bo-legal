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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.DocumentStatusService;
import world.inclub.bo_legal_microservice.domain.models.*;
import world.inclub.bo_legal_microservice.domain.request.ApiResponse;
import world.inclub.bo_legal_microservice.domain.request.StatusRequest;

@RestController
@RequestMapping("/api/v1/document/status")
@Slf4j
public class DocumentStatusController {
    
    @Autowired 
    private DocumentStatusService dsr;

    @GetMapping("/all")
    @Validated    
    Mono<ResponseEntity<ApiResponse<List<DocumentStatus>>>> 
    getAllDocumentStatus() 
    {
        log.info("getAllDocumentStatus: Retrieving all document status");

        return dsr.findAll()
        .collectList()
        .map(status -> {
            ApiResponse<List<DocumentStatus>> response = new ApiResponse<>(status, "Document status retrieved successfully");
            log.debug("getAllDocumentStatus: response: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen tarifas de documentos")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    @PostMapping("/add")
    @Validated
    Mono<ResponseEntity<ApiResponse<DocumentStatus>>>
    addStatus( @RequestBody @NotNull @Valid StatusRequest request)
    {
        log.info("addStatus: Adding new document status");
        
        return dsr.add(request)
        .map(status -> {
            ApiResponse<DocumentStatus> response = new ApiResponse<>(status, "Document status added successfully");
            log.debug("addStatus: response: {}", response.toString());
            return ResponseEntity.ok(response);
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Document status already exists")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }
    
    @PostMapping("/edit/{Id}")
    @Validated    
    Mono<ResponseEntity<ApiResponse<DocumentStatus>>> 
    editStatus(
        @PathVariable @NotNull @Valid Integer Id,
        @RequestBody @NotNull @Valid StatusRequest request)    
    {                
        log.info("editStatus: Editing document status with id: {}", Id);
        
        return
        dsr.findStatus(Id)
        .hasElement()
        .flatMap(existe -> {
            if (existe) {   
                log.info("editStatus: Document status exists, updating...");
                return dsr.update(request, Id)  
                .map(status -> {
                    ApiResponse<DocumentStatus> response = new ApiResponse<>(status, "Document status updated successfully");
                    log.debug("editStatus: response: {}", response.toString());
                    return ResponseEntity.ok(response);
                });
            } 
            log.error("editStatus: Document status does not exist");
            return Mono.error(new IllegalArgumentException("Document status does not exist"));            
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Document status does not exist")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    @PostMapping("/delete/{Id}")
    @Validated  
    Mono<ResponseEntity<ApiResponse<DocumentStatus>>>
    deleteStatus(
        @PathVariable @NotNull @Valid Integer Id)    
    {                
        log.info("deleteStatus: Deleting document status with id: {}", Id);
        
        return
        dsr.findStatus(Id)
        .hasElement()
        .flatMap(existe -> {
            if (existe) {   
                log.info("deleteStatus: Document status exists, deleting...");
                return dsr.delete(Id)
                .map(status -> {
                    ApiResponse<DocumentStatus> response = new ApiResponse<>(status, "Document status deleted successfully");
                    log.debug("deleteStatus: response: {}", response.toString());
                    return ResponseEntity.ok(response);
                });
            } 
            log.error("deleteStatus: Document status does not exist");
            return Mono.error(new IllegalArgumentException("Document status does not exist"));
        })
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Document status does not exist")))
        .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }
}
