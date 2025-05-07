package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.Document;


public interface DocumentRepository extends R2dbcRepository<Document, Integer> {
    Flux<Document> findAllByDocumentTypeId(Integer documentTypeId);
    Mono<Document> findByDocumentKey(String documentKey);

    @Query(
        "SELECT " +
        "d.id, d.document_key, d.status, d.document_type_id, d.created_at, d.modified_at, d.document_target_key, " + 
        "d.image_url, d.document_url, d.document_voucher_key, d.user_id, d.user_date, d.user_real_name, d.user_dni, " + 
        "d.user_local, d.user_local_type, d.portfolio_name, d.user_panel_id, d.legalization_type, d.price, " + 
        "cr.categorie_item_name as user_local_name, cr2.categorie_item_name as legalization_name " +
        "FROM core.document d " +
        "LEFT JOIN core.categorie cr ON d.user_local_type = cr.categorie_item_id AND cr.categorie_name = 'LOCAL_TYPE' " +
        "LEFT JOIN core.categorie cr2 ON d.legalization_type = cr2.categorie_item_id AND cr2.categorie_name = 'LEGALIZATION_TYPE' " +
        "WHERE d.document_type_id < 100 AND d.document_key = :key AND d.status > 0")
    Mono<Document> findByDocumentKeyVoucher(@Param("key") String key);

    @Query(
        "SELECT " +
        "d.id, d.document_key, d.status, d.document_type_id, d.created_at, d.modified_at, d.document_target_key, " + 
        "d.image_url, d.document_url, d.document_voucher_key, d.user_id, d.user_date, d.user_real_name, d.user_dni, " + 
        "d.user_local, d.user_local_type, d.portfolio_name, d.user_panel_id, d.legalization_type, d.price, " + 
        "cr.categorie_item_name as user_local_name, cr2.categorie_item_name as legalization_name " +
        "FROM core.document d " +
        "LEFT JOIN core.categorie cr ON d.user_local_type = cr.categorie_item_id AND cr.categorie_name = 'LOCAL_TYPE' " +
        "LEFT JOIN core.categorie cr2 ON d.legalization_type = cr2.categorie_item_id AND cr2.categorie_name = 'LEGALIZATION_TYPE' " +
        "WHERE d.document_type_id > 100 AND d.document_key = :key AND d.status > 0")
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

    @Query(
        "SELECT " +
        "d.id, d.document_key, d.status, d.document_type_id, d.created_at, d.modified_at, d.document_target_key, " + 
        "d.image_url, d.document_url, d.document_voucher_key, d.user_id, d.user_date, " + 
        "d.user_local, d.user_local_type, d.user_panel_id, d.legalization_type, d.price, " + 
        "cr.categorie_item_name as user_local_name, cr2.categorie_item_name as legalization_name, " +        
        "c.categorie_item_name as document_type_name, " +
        "cd.description as status_description, " +
        "CASE WHEN d.document_type_id = 50 THEN doc.portfolio_name " +
        "ELSE d.portfolio_name " +
        "END AS portfolio_name, " +
        "CASE WHEN d.document_type_id = 50 THEN doc.user_id " +
        "ELSE d.user_id " +
        "END AS user_id, " +
        "CASE WHEN d.document_type_id = 50 THEN doc.user_real_name " +
        "ELSE d.user_real_name " +
        "END AS user_real_name, " +
        "CASE WHEN d.document_type_id = 50 THEN doc.user_dni " +
        "ELSE d.user_dni " +
        "END AS user_dni " +
        "FROM core.document d " +
        "LEFT JOIN core.document doc ON doc.document_type_id > 100 AND d.document_target_key = doc.document_key AND doc.status > 0 " +        
        "LEFT JOIN core.categorie c ON d.document_type_id = c.categorie_item_id AND c.categorie_name = 'DOCUMENT_TYPE_ID' " +
        "LEFT JOIN core.document_status_client_description cd ON d.document_type_id = cd.document_type_id AND d.status = cd.status AND cd.active = 1 " +       
        "LEFT JOIN core.categorie cr ON d.user_local_type = cr.categorie_item_id AND cr.categorie_name = 'LOCAL_TYPE' " +
        "LEFT JOIN core.categorie cr2 ON d.legalization_type = cr2.categorie_item_id AND cr2.categorie_name = 'LEGALIZATION_TYPE' " +
        "WHERE d.status > 0 AND d.document_type_id = :documentTypeId")
    Flux<Document> findAllExtraByDocumentTypeId(@Param("documentTypeId") Integer documentTypeId);
}
