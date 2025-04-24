package world.inclub.bo_legal_microservice.controller;

import java.io.Console;
import java.time.LocalDateTime;

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
import world.inclub.bo_legal_microservice.model.*;
import world.inclub.bo_legal_microservice.repository.*;

@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {

    @Autowired
    private DocumentRepository dr;
    @Autowired 
    private DocumentHistoryRepository dhr;
    @Autowired 
    private DocumentStatusRepository dsr;

    @PostMapping("/add")
    Mono<ResponseEntity<String>> addDocument(@RequestBody Document doc)
    {
        if( doc.getDocumentTypeId() > 3)
            return Mono.error(new IllegalArgumentException("Tipo de documento no permitido"));

        return dr.findByDocumentKey(doc.getDocumentKey())
            .hasElement()
            .flatMap( existe -> {
                if( existe )
                    return Mono.error(new IllegalArgumentException("Documento ya existe"));
                else {
                    // Crear nuevo documento en estado pendiente        
                    Document nuevoDoc = doc.toBuilder()
                    .status(1) // nuevo estado        
                    .createdAt(LocalDateTime.now()) // fecha de modificación actual
                    .modifiedAt(LocalDateTime.now()) // fecha de modificación actual
                    .build();

                    // Crear entrada de historial
                    DocumentHistory dh = new DocumentHistory();
                    dh.setDocumentKey(doc.getDocumentKey());
                    dh.setStatus(1);
                    dh.setReasonText("Nuevo Voucher");
                    dh.setReasonType(0);
                    dh.setCreatedAt(LocalDateTime.now());
                    dh.setUserPanelId(doc.getUserPanelId());

                    return dr.save(nuevoDoc)
                        .then(dhr.save(dh))
                        .thenReturn(ResponseEntity.ok("Se ha creado el voucher para el documento " + doc.getDocumentKey() + " con éxito"));
                }
            }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));              
    }

    @PostMapping("/add/solicitud/{documentTypeId}")
    Mono<ResponseEntity<String>> addDocumentSolicitud(
        @RequestBody Document doc, 
        @PathVariable Integer documentTypeId)
    {        
        if( doc.getDocumentTypeId() > 2)
            return Mono.error(new IllegalArgumentException("Tipo de documento no permitido"));

        return dr.findByDocumentKey(doc.getDocumentKey())
            .hasElement()
            .flatMap( existe -> {
                if( existe )
                    return Mono.error(new IllegalArgumentException("Documento ya existe"));
                else {
                    // Crear nuevo documento en estado pendiente        
                    Document nuevoDoc = doc.toBuilder()
                    .status(1) // nuevo estado        
                    .createdAt(LocalDateTime.now()) // fecha de modificación actual
                    .modifiedAt(LocalDateTime.now()) // fecha de modificación actual
                    .build();

                    // Crear entrada de historial
                    DocumentHistory dh = new DocumentHistory();
                    dh.setDocumentKey(doc.getDocumentKey());
                    dh.setStatus(1);
                    dh.setReasonText("Nuevo Voucher de Solicitud de Legalización");
                    dh.setReasonType(0);
                    dh.setCreatedAt(LocalDateTime.now());
                    dh.setUserPanelId(doc.getUserPanelId());

                    return dr.save(nuevoDoc)
                        .then(dhr.save(dh))
                        .thenReturn(ResponseEntity.ok("Se ha creado el voucher para el documento " + doc.getDocumentKey() + " con éxito"));
                }
            }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));              
    }

    @PostMapping("/add/rectificacion")
    Mono<ResponseEntity<String>> addDocumentRectificacion(
        @RequestBody Document doc)
    {
        if( doc.getDocumentTypeId() > 3)
            return Mono.error(new IllegalArgumentException("Tipo de documento no permitido"));

        return dr.findByDocumentKey(doc.getDocumentKey())
            .hasElement()
            .flatMap( existe -> {
                if( existe )
                    return Mono.error(new IllegalArgumentException("Documento ya existe"));
                else {
                    // Crear nuevo documento en estado pendiente        
                    Document nuevoDoc = doc.toBuilder()
                    .status(1) // nuevo estado        
                    .createdAt(LocalDateTime.now()) // fecha de modificación actual
                    .modifiedAt(LocalDateTime.now()) // fecha de modificación actual
                    .build();

                    // Crear entrada de historial
                    DocumentHistory dh = new DocumentHistory();
                    dh.setDocumentKey(doc.getDocumentKey());
                    dh.setStatus(1);
                    dh.setReasonText("Nuevo Voucher");
                    dh.setReasonType(0);
                    dh.setCreatedAt(LocalDateTime.now());
                    dh.setUserPanelId(doc.getUserPanelId());

                    return dr.save(nuevoDoc)
                        .then(dhr.save(dh))
                        .thenReturn(ResponseEntity.ok("Se ha creado el voucher para el documento " + doc.getDocumentKey() + " con éxito"));
                }
            }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));              
    }

    @GetMapping("/all")
    Flux<Document> getAllDocuments() {
        return dr.findAll();
    }

    @GetMapping("/all/{documentTypeId}")
    Flux<Document> getAllDocumentsByType(@PathVariable Integer documentTypeId) {
        return dr.findAllByDocumentTypeId(documentTypeId);
    }

    @GetMapping("/{documentKey}")
    Mono<Document> getByDocumentKey(@PathVariable String documentKey) {
        return dr.findByDocumentKeyProcess(documentKey)
        .switchIfEmpty(
            dr.findByDocumentKeyVoucher(documentKey).switchIfEmpty(
                Mono.error(new IllegalArgumentException("Documento no encontrado"))
            )
        );            
    }

    @PostMapping("/change")
    public Mono<ResponseEntity<String>> changeStatusDocument(
            @RequestBody DocumentChangeStatusRequest request) {

        if( request.getStatus() < 4)
                return Mono.error(new IllegalArgumentException("Estado no permitido"));

        return dsr.findById(request.getStatus())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Estado no configurado")))
            .then(dr.findByDocumentKeyProcess(request.getDocumentKey()) )
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Documento no encontrado")))
            .flatMap(documento -> {

                if( documento.getStatus() == request.getStatus()) return Mono.error(new IllegalArgumentException("El documento ya se encuentra en el estado solicitado"));
                if( documento.getStatus() == 6) return Mono.error(new IllegalArgumentException("No se puede modificar el estado final (6) Atendido "));
                documento.setStatus(request.getStatus());
                documento.setModifiedAt(LocalDateTime.now());                

                // Crear entrada de historial
                DocumentHistory dh = new DocumentHistory();
                dh.setDocumentKey(documento.getDocumentKey());
                dh.setStatus(request.getStatus());
                dh.setReasonText(request.getReasonText());
                dh.setReasonType(request.getReasonType());
                dh.setCreatedAt(LocalDateTime.now());
                dh.setUserPanelId(request.getUserPanelId());

                return dr.save(documento)
                    .then(dhr.save(dh))
                    .thenReturn(ResponseEntity.ok("Estado del documento actualizado y guardado en el Historial"));
            })
            .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<String>> approveDocument(            
            @RequestBody DocumentChangeStatusRequest request) {

        return 
            dr.findByDocumentKey(request.getDocumentKey())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe")))
            .filter(doc -> doc.getStatus() == 1) // solo pasa si el status es 1
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el estado esperado")))
            .filter(doc -> doc.getDocumentTypeId() <= 2) // solo pasa si el tipo es un voucher de solicitud
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el tipo esperado")))        
            .flatMap(doc -> {
                // Actualizar estado
                doc.setStatus(2); // estado aprobado
                doc.setModifiedAt(LocalDateTime.now());

                // Crear entrada de historial
                DocumentHistory dh = new DocumentHistory();
                dh.setDocumentKey(doc.getDocumentKey());
                dh.setStatus(2); // Estado aprobado = 2
                dh.setReasonText("Voucher aprobado");
                dh.setReasonType(0);
                dh.setCreatedAt(LocalDateTime.now());
                dh.setUserPanelId(request.getUserPanelId());
                
                // Crear nuevo documento en estado pendiente
                Document nuevoDoc = new Document();
                nuevoDoc = doc.toBuilder().build();
                nuevoDoc.setStatus(1); // nuevo estado
                nuevoDoc.setDocumentTypeId(doc.getDocumentTypeId()+100);
                nuevoDoc.setCreatedAt(LocalDateTime.now()); // fecha de modificación actual
                nuevoDoc.setModifiedAt(LocalDateTime.now()); // fecha de modificación actual
                nuevoDoc.setId(null);

                return dr.save(doc)
                    .then(dr.save(nuevoDoc))
                    .then(dhr.save(dh))
                    .thenReturn(ResponseEntity.ok("Se ha aprobado el voucher y se ha generado la solicitud con éxito"));
            })
            .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/{documentKey}/reject")
    public Mono<ResponseEntity<String>> rejectDocument(
            @PathVariable String documentKey,
            @RequestBody DocumentChangeStatusRequest request) {

        return 
            dr.findByDocumentKey(documentKey)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe")))
            .filter(doc -> doc.getStatus() == 1) // solo pasa si el status es 1
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el estado esperado")))
            .filter(doc -> doc.getDocumentTypeId() <= 2) // solo pasa si el tipo es un voucher de solicitud
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el tipo esperado")))
            .flatMap(doc -> {
                // Actualizar estado
                doc.setStatus(3); // estado rechazado
                doc.setModifiedAt(LocalDateTime.now());

                // Crear entrada de historial
                DocumentHistory dh = new DocumentHistory();
                dh.setDocumentKey(doc.getDocumentKey());
                dh.setStatus(3);
                dh.setReasonText(request.getReasonText());
                dh.setReasonType(request.getReasonType());
                dh.setCreatedAt(LocalDateTime.now());
                dh.setUserPanelId(request.getUserPanelId());

                return dr.save(doc)                    
                    .then(dhr.save(dh))
                    .thenReturn(ResponseEntity.ok("Se ha rechazado el voucher"));
            })
            .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @GetMapping("/history/{documentKey}")
    Flux<DocumentHistory> getHistoryByKey(
        @PathVariable String documentKey) {
        return dhr.findByDocumentKey(documentKey);
    }
}
