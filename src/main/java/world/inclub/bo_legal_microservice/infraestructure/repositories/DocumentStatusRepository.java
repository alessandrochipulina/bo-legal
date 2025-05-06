package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.DocumentStatus;

public interface DocumentStatusRepository extends R2dbcRepository<DocumentStatus, Integer> {
    Mono<DocumentStatus> findById(Integer Id); // o findById si es Integer id
}
