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

    @Query("SELECT * FROM core.document WHERE document_type_id < 100 AND document_key = :key AND status > 0")
    Mono<Document> findByDocumentKeyVoucher(@Param("key") String key);

    @Query("SELECT * FROM core.document WHERE document_type_id > 100 AND document_key = :key AND status > 0")
    Mono<Document> findByDocumentKeyProcess(@Param("key") String key);

    @Query("SELECT EXISTS( SELECT 1 FROM core.document WHERE document_type_id = 50 AND status = 1 AND document_target_key = :key ) as resultado")
    Mono<Boolean> findByExistsRectificacionPendingKey(@Param("key") String key);

    @Query("SELECT * FROM core.document WHERE userId = :userId AND status > 0")
    Flux<Document> findAllByDocumentUserId(@Param("userId") String key);
}
