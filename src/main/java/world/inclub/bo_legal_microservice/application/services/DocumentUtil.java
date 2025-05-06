package world.inclub.bo_legal_microservice.application.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.Document;
import world.inclub.bo_legal_microservice.domain.models.DocumentHistory;
import world.inclub.bo_legal_microservice.domain.request.DocumentChangeStatusRequest;
import world.inclub.bo_legal_microservice.infraestructure.config.AppProperties;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentHistoryRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentRepository;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentStatusRepository;

@Component
public class DocumentUtil {

    @Autowired
    private DocumentRepository dr;
    @Autowired 
    private DocumentHistoryRepository dhr;
    @Autowired 
    private DocumentStatusRepository dsr;
    @Autowired
    private AppProperties app;

    public DocumentHistory saveSystemHistory(
        String key, 
        String reason, 
        Integer status, 
        Integer user) {
        DocumentHistory dh = new DocumentHistory();
        dh.setDocumentKey(key);
        dh.setStatus(status);
        dh.setReasonText(reason);
        dh.setReasonType(0);
        dh.setCreatedAt(LocalDateTime.now());
        dh.setUserPanelId(user);
        return dh;
    }

    public Document newDocument(Document doc) {
        // Crear nuevo documento en estado pendiente        
        return doc.toBuilder()
        .status(1) // nuevo estado        
        .createdAt(LocalDateTime.now()) // fecha de modificación actual
        .modifiedAt(LocalDateTime.now()) // fecha de modificación actual
        .id(null)
        .build();
    }

    public Mono<ResponseEntity<String>> addDocumentSolicitud(
        @RequestBody Document doc, 
        @PathVariable Integer documentTypeId){

            if( documentTypeId >= app.getType().getVoucherrectificacion() ) return Mono.error(new IllegalArgumentException("Tipo de documento no permitido"));

            return dr.findByDocumentKey(doc.getDocumentKey()).hasElement().flatMap( existe -> {
                if( existe ) return Mono.error(new IllegalArgumentException("Documento ya existe"));
                else {
                    // Crear nuevo documento en estado pendiente        
                    Document nuevoDoc = this.newDocument(doc);
                    nuevoDoc.setDocumentTypeId(documentTypeId);
                    // Crear entrada de historial
                    DocumentHistory dh = this.saveSystemHistory( doc.getDocumentKey(), "Nuevo Voucher de Solicitud de Legalización",1, doc.getUserPanelId());
                    // Grabar los datos
                    return dr.save(nuevoDoc).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Se ha creado el voucher (solicitud) " + doc.getDocumentKey() + " con éxito"));
                }
            }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage()))); 
    }
    
    public Mono<ResponseEntity<String>> addDocumentRectificacion(
        @RequestBody Document doc)
    {
        return 
        dr.findByExistsRectificacionPendingKey(doc.getDocumentTargetKey())
        .flatMap( existe -> {
            if( existe ) return Mono.just( ResponseEntity.badRequest().body("La solicitud de la referencia ya cuenta con un voucher en estado pendiente") );
            return
            dr.findByDocumentKeyProcess(doc.getDocumentTargetKey())            
            .switchIfEmpty(Mono.error(new IllegalArgumentException("La solicitud referenciada X no existe")))
            .flatMap( target -> {
                // Crear nuevo documento en estado pendiente        
                if( target.getDocumentTypeId() < app.getType().getSolicitudcertificado()) Mono.error(new IllegalArgumentException("La solicitud referenciada no es del tipo requerido"));
                if( target.getStatus() <= app.getStatus().getAtendido()) Mono.error(new IllegalArgumentException("El estado de la solicitud referenciada no permite rectificar el lugar de recojo"));
                // Crea un nuevo documento
                doc.setDocumentTypeId(app.getType().getVoucherrectificacion());
                Document voucher = this.newDocument(doc);
                // Crear entrada de historial
                DocumentHistory dh = this.saveSystemHistory( doc.getDocumentKey(), "Nuevo Voucher de Rectificación de Lugar de Recojo",app.getStatus().getPendiente(), doc.getUserPanelId());

                return
                dr.save(voucher).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Se ha creado el voucher (rectificacion) " + doc.getDocumentKey() + " para " + doc.getDocumentTargetKey() + " con éxito"));
            })
            .onErrorResume(
                e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
            );            
        });
    }

    public Mono<ResponseEntity<String>> changeStatusDocument(
        @RequestBody DocumentChangeStatusRequest request) 
    {
        if( request.getStatus() < app.getStatus().getAtendido()) return Mono.error(new IllegalArgumentException("Estado no permitido"));

        return dsr.findById(request.getStatus())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Estado no configurado")))
        .then(dr.findByDocumentKeyProcess(request.getDocumentKey()) )
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Documento no encontrado")))
        .flatMap(doc -> 
        {
            if( doc.getStatus() == request.getStatus()) return Mono.error(new IllegalArgumentException("El documento ya se encuentra en el estado solicitado"));
            if( doc.getStatus() == app.getStatus().getAtendido()) return Mono.error(new IllegalArgumentException("No se puede modificar el estado final Atendido "));
            doc.setStatus(request.getStatus());
            doc.setModifiedAt(LocalDateTime.now());                

            // Crear entrada de historial
            DocumentHistory dh = this.saveSystemHistory( doc.getDocumentKey(), request.getReasonText(), request.getStatus(), doc.getUserPanelId()); 
            dh.setReasonType(request.getReasonType());
            // Grabar contenido
            return dr.save(doc).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Estado del documento actualizado y guardado en el Historial"));
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    public Mono<ResponseEntity<String>> approveDocument(            
            @RequestBody DocumentChangeStatusRequest request) 
    {
        return 
        dr.findByDocumentKeyVoucher(request.getDocumentKey())
        .flatMap(voucher -> {    
            if( voucher==null ) return Mono.error(new IllegalArgumentException("Documento ya existe"));
            if( voucher.getStatus() != app.getStatus().getPendiente()) return Mono.just(ResponseEntity.badRequest().body("El voucher no se encuentra en estado pendiente"));
            // Actualizar estado
            voucher.setStatus(app.getStatus().getAprobado()); // estado aprobado
            voucher.setModifiedAt(LocalDateTime.now());
            // Crear entrada de historial
            DocumentHistory dh_voucher = this.saveSystemHistory( voucher.getDocumentKey(), "", app.getStatus().getAprobado() ,request.getUserPanelId());                 
            if( voucher.getDocumentTypeId() == app.getType().getVoucherrectificacion() ) dh_voucher.setReasonText("Voucher de rectificación aprobado");
            else dh_voucher.setReasonText("Voucher de solicitud aprobado");
                
            if( voucher.getDocumentTypeId() == app.getType().getVoucherrectificacion() ) 
            {
                return 
                dr.findByDocumentKeyProcess(voucher.getDocumentTargetKey())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La solicitud de la referencia no existe")))
                .flatMap( target -> {                    
                    if( target.getStatus() == app.getStatus().getAprobado() ) return Mono.error(new IllegalArgumentException("El documento de referencia está en estado final (Atendido)"));
                    String reason = "Se ha cambiado el Lugar de Recojo para esta solicitud";
                    if( target.getStatus() == app.getStatus().getRecojo() ) {
                        target.setStatus(app.getStatus().getProceso()); // En Proceso
                        reason = "Se ha cambiado el Lugar de Recojo y el Estado para esta solicitud";
                    }
                    target.setUserLocal(voucher.getUserLocal());
                    target.setUserLocalType(voucher.getUserLocalType());
                    // Crear entrada del historial
                    DocumentHistory dh_target = this.saveSystemHistory( target.getDocumentKey(), reason, target.getStatus(), request.getUserPanelId());
                                                        
                    return dr.save(voucher).then(dr.save(target)).then(dhr.save(dh_voucher)).then(dhr.save(dh_target))
                        .thenReturn(ResponseEntity.ok("Se ha aprobado el voucher y se ha rectificado el lugar de recojo con éxito"));
                })
                .onErrorResume(e -> {
                        return Mono.just(ResponseEntity.badRequest().body(e.getMessage()));
                    }
                );
            }
            else {
                // Crear nuevo documento en estado pendiente
                Document solicitud = this.newDocument(voucher);                    
                solicitud.setDocumentTypeId(voucher.getDocumentTypeId()+100);
                // Crear entrada de historial
                DocumentHistory dh_solicitud = this.saveSystemHistory( solicitud.getDocumentKey(), "Nueva Solicitud de Legalización", app.getStatus().getPendiente(), request.getUserPanelId());

                return dr.save(voucher).then(dr.save(solicitud)).then(dhr.save(dh_voucher)).then(dhr.save(dh_solicitud))
                    .thenReturn(ResponseEntity.ok("Se ha aprobado el voucher y se ha generado la solicitud con éxito"));
            }            
        })
        .onErrorResume(e -> {
                return Mono.just(ResponseEntity.badRequest().body(e.getMessage()));
            }
        );
    }

    public Mono<ResponseEntity<String>> rejectDocument(
            @PathVariable String documentKey,
            @RequestBody DocumentChangeStatusRequest request) 
    {
        return 
        dr.findByDocumentKeyVoucher(documentKey)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no existe")))
        .filter(doc -> doc.getStatus() == app.getStatus().getPendiente()) // solo pasa si el status es 1
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el estado esperado")))
        .filter(doc -> doc.getDocumentTypeId() <= app.getType().getVoucherrectificacion()) // solo pasa si el tipo es un voucher de solicitud
        .switchIfEmpty(Mono.error(new IllegalArgumentException("El documento no tiene el tipo esperado")))
        .flatMap(doc -> {
            // Actualizar estado
            doc.setStatus(app.getStatus().getRechazado()); // estado rechazado
            doc.setModifiedAt(LocalDateTime.now());
            // Crear entrada de historial
            DocumentHistory dh = this.saveSystemHistory( doc.getDocumentKey(), request.getReasonText(), app.getStatus().getRechazado(), request.getUserPanelId());
            dh.setReasonType(request.getReasonType());                

            return dr.save(doc).then(dhr.save(dh)).thenReturn(ResponseEntity.ok("Se ha rechazado el voucher"));
        })
        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}
