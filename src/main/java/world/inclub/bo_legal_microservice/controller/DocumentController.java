package world.inclub.bo_legal_microservice.controller;

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
import world.inclub.bo_legal_microservice.config.AppProperties;
import world.inclub.bo_legal_microservice.logic.*;
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
    @Autowired
    private DocumentUtil du;
    @Autowired
    private AppProperties app;

    @PostMapping("/add/solicitud/{documentTypeId}")
    Mono<ResponseEntity<String>> addDocumentSolicitud(
        @RequestBody Document doc, 
        @PathVariable Integer documentTypeId)
    {        
        if( documentTypeId >= app.getType().getVoucherrectificacion() ) return Mono.error(new IllegalArgumentException("Tipo de documento no permitido"));

        return dr.findByDocumentKey(doc.getDocumentKey()).hasElement().flatMap( existe -> {
            if( existe ) return Mono.error(new IllegalArgumentException("Documento ya existe"));
            else {
                // Crear nuevo documento en estado pendiente        
                Document nuevoDoc = du.newDocument(doc);
                nuevoDoc.setDocumentTypeId(documentTypeId);
                // Crear entrada de historial
                DocumentHistory dh = du.saveSystemHistory( doc.getDocumentKey(), "Nuevo Voucher de Solicitud de Legalización",1, doc.getUserPanelId());
                // Grabar los datos
                return dr.save(nuevoDoc).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Se ha creado el voucher (solicitud) " + doc.getDocumentKey() + " con éxito"));
            }
        }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));              
    }

    @PostMapping("/add/rectificacion")
    Mono<ResponseEntity<String>> addDocumentRectificacion(
        @RequestBody Document doc)
    {
        return dr.findByDocumentKey(doc.getDocumentTargetKey())            
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento referenciado no existe")))
        .flatMap( target -> {
            // Crear nuevo documento en estado pendiente        
            if( target.getDocumentTypeId() <= 100) Mono.error(new IllegalArgumentException("El documento referenciado no es del tipo requerido"));
            if( target.getStatus() <= 4) Mono.error(new IllegalArgumentException("El estado del documento referenciado no permite rectificar el lugar de recojo"));
            // Crea un nuevo documento
            doc.setDocumentTypeId(50);
            Document voucher = du.newDocument(doc);
            // Crear entrada de historial
            DocumentHistory dh = du.saveSystemHistory( doc.getDocumentKey(), "Nuevo Voucher de Rectificación de Lugar de Recojo",1, doc.getUserPanelId());            

            return dr.save(voucher).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Se ha creado el voucher (rectificacion) " + doc.getDocumentKey() + " para " + doc.getDocumentTargetKey() + " con éxito"));
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));              
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
        @RequestBody DocumentChangeStatusRequest request) 
    {
        if( request.getStatus() < 4) return Mono.error(new IllegalArgumentException("Estado no permitido"));

        return dsr.findById(request.getStatus())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Estado no configurado")))
        .then(dr.findByDocumentKeyProcess(request.getDocumentKey()) )
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Documento no encontrado")))
        .flatMap(doc -> 
        {
            if( doc.getStatus() == request.getStatus()) return Mono.error(new IllegalArgumentException("El documento ya se encuentra en el estado solicitado"));
            if( doc.getStatus() == 4) return Mono.error(new IllegalArgumentException("No se puede modificar el estado final (4) Atendido "));
            doc.setStatus(request.getStatus());
            doc.setModifiedAt(LocalDateTime.now());                

            // Crear entrada de historial
            DocumentHistory dh = du.saveSystemHistory( doc.getDocumentKey(), request.getReasonText(), request.getStatus(), doc.getUserPanelId()); 
            dh.setReasonType(request.getReasonType());
            // Grabar contenido
            return dr.save(doc).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Estado del documento actualizado y guardado en el Historial"));
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<String>> approveDocument(            
            @RequestBody DocumentChangeStatusRequest request) 
    {
        return 
            dr.findByDocumentKey(request.getDocumentKey())
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El voucher no existe")))
            .filter(voucher -> voucher.getStatus() == 1) // solo pasa si el status es 1
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El voucher no está en estado pendiente")))
            .filter(voucher -> voucher.getDocumentTypeId() < 100) // solo pasa si el tipo es un voucher
            .switchIfEmpty(Mono.error(new IllegalArgumentException("El voucher no tiene el tipo esperado")))        
            .flatMap(voucher -> {
                // Actualizar estado
                voucher.setStatus(2); // estado aprobado
                voucher.setModifiedAt(LocalDateTime.now());

                // Crear entrada de historial
                DocumentHistory dh_voucher = du.saveSystemHistory( voucher.getDocumentKey(), "", 2, request.getUserPanelId());                 
                if( voucher.getDocumentTypeId() == 50 ) dh_voucher.setReasonText("Voucher de rectificación aprobado");
                else dh_voucher.setReasonText("Voucher de solicitud aprobado");
                
                if( voucher.getDocumentTypeId() == 50 ) 
                {
                    dr.findByDocumentKeyVoucher(voucher.getDocumentTargetKey())
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento de la referencia no existe")))
                    .flatMap(
                        target -> {
                            if( target.getDocumentTypeId() < 100 ) return Mono.error(new IllegalArgumentException("El documento de referencia no tiene el tipo esperado"));
                            if( target.getStatus() == 4 ) return Mono.error(new IllegalArgumentException("El documento de referencia está en estado final (Atendido)"));
                            if( target.getStatus() == 5) target.setStatus(7); // En Proceso                            
                            target.setUserLocal(voucher.getUserLocal());
                            target.setUserLocalType(voucher.getUserLocalType());
                            // Crear entrada del historial
                            DocumentHistory dh_target = du.saveSystemHistory( target.getDocumentKey(), "Se ha cambiado el Lugar de Recojo para esta solicitud", voucher.getStatus(), request.getUserPanelId());
                                                        
                            return dr.save(voucher).then(dr.save(target)).then(dhr.save(dh_voucher)).then(dhr.save(dh_target))
                            .thenReturn(ResponseEntity.ok("Se ha aprobado el voucher y se ha rectificado el lugar de recojo con éxito"));
                        }
                    )
                    .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));                    
                }
                else {
                    // Crear nuevo documento en estado pendiente
                    Document solicitud = du.newDocument(voucher);                    
                    solicitud.setDocumentTypeId(voucher.getDocumentTypeId()+100);
                    // Crear entrada de historial
                    DocumentHistory dh_solicitud = du.saveSystemHistory( solicitud.getDocumentKey(), "Nueva Solicitud de Legalización", 1, request.getUserPanelId());

                    return dr.save(voucher).then(dr.save(solicitud)).then(dhr.save(dh_voucher)).then(dhr.save(dh_solicitud))
                        .thenReturn(ResponseEntity.ok("Se ha aprobado el voucher y se ha generado la solicitud con éxito"));
                }
                return Mono.just(ResponseEntity.ok().body(""));
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
            .filter(doc -> doc.getDocumentTypeId() < 100) // solo pasa si el tipo es un voucher de solicitud
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
