package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import world.inclub.bo_legal_microservice.domain.models.DocumentHistory;


public interface DocumentHistoryRepository extends R2dbcRepository<DocumentHistory, Integer> {
    Flux<DocumentHistory> findByDocumentKey(String documentKey);
}
