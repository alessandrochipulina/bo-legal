package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.DocumentStatus;

public interface DocumentStatusRepository extends R2dbcRepository<DocumentStatus, Integer> {
        
    @SuppressWarnings("null")
    @Query("SELECT * FROM core.document_status WHERE active = 1")
    Flux<DocumentStatus> findAll();

    @SuppressWarnings("null")
    @Query("SELECT * FROM core.document_status WHERE active = 1 AND id = :id")
    Mono<DocumentStatus> findById(@NotNull Integer id);
}
