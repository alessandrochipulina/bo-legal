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

    @Query(
        "SELECT DISTINCT ON (d.document_key) document_key, d.document_type_id, d.status, d.modified_at, " +
        "d.document_url, c.categorie_item_name as document_type_name, d.portfolio_name, d.legalization_type, " +    
        "cd.description as status_description, d.user_local " +
        "FROM core.document d " +
        "INNER JOIN core.categorie c ON d.document_type_id = c.categorie_item_id AND c.categorie_name = 'DOCUMENT_TYPE_ID' " +
        "LEFT JOIN core.document_status_client_description cd ON d.document_type_id = cd.document_type_id AND d.status = cd.status AND cd.active = 1 " +
        "WHERE d.user_id = :userId AND d.status > 0 " +
        "ORDER BY d.document_key, d.document_type_id DESC ")
    Flux<Document> findAllByDocumentUserId(@Param("userId") String key);
}
