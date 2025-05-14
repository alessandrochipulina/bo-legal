package world.inclub.bo_legal_microservice.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.DocumentStatus;
import world.inclub.bo_legal_microservice.infraestructure.config.AppProperties;
import world.inclub.bo_legal_microservice.infraestructure.repositories.DocumentStatusRepository;

@Component
public class DocumentStatusService {

    @Autowired
    private DocumentStatusRepository dsr;
    @Autowired
    private AppProperties app;

    public Mono<DocumentStatus> 
    update(DocumentStatus ds)    
    {
        return this.findStatus(ds.getId())
            .flatMap(status -> {
                status.setColor(ds.getColor());
                status.setDescription(ds.getDescription());      
                return dsr.save(status).thenReturn(status);
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encuentra el status")))
            .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    public Mono<DocumentStatus>
    add(DocumentStatus ds)    
    {
        ds.setId(null);
        return this.findStatus(0)
            .hasElement()
            .flatMap(existe -> {
                if (!existe) {   
                    return dsr.save(ds).thenReturn(ds);
                } else {
                    return Mono.error(new IllegalArgumentException("El status ya existe"));
                }
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encuentra el status")))
            .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    public Mono<DocumentStatus>
    delete(Integer status) 
    {
        if( status <= app.getStatus().getAtendido()) 
            Mono.error(new IllegalArgumentException("El estado no puede ser eliminado"));
        return this.findStatus(status)
            .flatMap(s -> {
                s.setActive(0);
                s.setIsDeleteable(0);
                return dsr.save(s).thenReturn(s);
            })
            .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encuentra el status")))
            .onErrorResume(e -> Mono.error(new IllegalArgumentException(e.getMessage())));
    }

    public Mono<DocumentStatus>
    findStatus(Integer status) 
    {
        return dsr.findById(status);
    }

    public Flux<DocumentStatus>
    findAll() 
    {        
        return dsr.findAll();
    }

}