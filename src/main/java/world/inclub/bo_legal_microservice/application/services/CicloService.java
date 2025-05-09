package world.inclub.bo_legal_microservice.application.services;

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

    public Mono<Void> deleteById(Integer id) {
        return repository.deleteById(id);
    }

    public Mono<Ciclo> update(Integer id, Ciclo updated) {
        return repository.findById(id)
                .flatMap(existing -> {
                    /* 
                    existing.setName(updated.getName());
                    existing.setStartAt(updated.getStartAt());
                    existing.setEndAt(updated.getEndAt());
                    */
                    // Actualiza los campos necesarios
                    return repository.save(existing);
                });
    }
}