package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.model.DocumentStatus;

public interface DocumentStatusRepository extends R2dbcRepository<DocumentStatus, Integer> {
    Mono<DocumentStatus> findById(Integer Id); // o findById si es Integer id
}
