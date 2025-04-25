package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.model.Document;


public interface DocumentRepository extends R2dbcRepository<Document, Integer> {
    Flux<Document> findAllByDocumentTypeId(Integer documentTypeId);
    Mono<Document> findByDocumentKey(String documentKey);

    @Query("SELECT * FROM core.document WHERE document_type_id < 100 AND document_key = :key")
    Mono<Document> findByDocumentKeyVoucher(String key);

    @Query("SELECT * FROM core.document WHERE document_type_id > 100 AND document_key = :key ")
    Mono<Document> findByDocumentKeyProcess(@Param("key") String key);
}
