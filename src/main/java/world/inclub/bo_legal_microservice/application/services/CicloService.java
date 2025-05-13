package world.inclub.bo_legal_microservice.application.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.Ciclo;
import world.inclub.bo_legal_microservice.infraestructure.repositories.CicloRepository;

@Service
public class CicloService {

    private final CicloRepository repository;

    public CicloService(CicloRepository repository) {
        this.repository = repository;
    }

    public Flux<Ciclo> findAll() {
        return repository.findAll();
    }

    public Mono<Ciclo> findById(Integer id) {
        return repository.findById(id);
    }

    public Mono<Ciclo> save(Ciclo ciclo) {
        return repository.save(ciclo);
    }

    public Mono<Ciclo> deleteById(Integer id) {
        return repository.findById(id)
                .flatMap(existing -> {                     
                    return repository.deleteById(id).thenReturn(existing);                    
                });
    }

    public Mono<Ciclo> update(Integer id, Ciclo updated) {
        return repository.findById(id)
                .flatMap(existing -> {                     
                    existing.setName(updated.getName());
                    existing.setLegalizationType(updated.getLegalizationType());                    
                    existing.setStatus(updated.getStatus());
                    existing.setAllDay(updated.getAllDay());
                    existing.setStartHourAt(updated.getStartHourAt());
                    existing.setEndHourAt(updated.getEndHourAt());
                    existing.setLocale(updated.getLocale());
                    existing.setDescription(updated.getDescription());
                    existing.setColor(updated.getColor());               
                    // completar con la fecha y hora actual
                    existing.setModifiedAt(LocalDateTime.now());
                    existing.setUserPanelId(updated.getUserPanelId());                    
                    existing.setStartAt(updated.getStartAt());
                    existing.setEndAt(updated.getEndAt());        
                    // Actualiza los campos necesarios
                    return repository.save(existing).thenReturn(existing);
                });
    }
}