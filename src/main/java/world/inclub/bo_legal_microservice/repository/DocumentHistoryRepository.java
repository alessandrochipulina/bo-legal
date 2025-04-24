package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import world.inclub.bo_legal_microservice.model.DocumentHistory;


public interface DocumentHistoryRepository extends R2dbcRepository<DocumentHistory, Integer> {
    Flux<DocumentHistory> findByDocumentKey(String documentKey);
}
