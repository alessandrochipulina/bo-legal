package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.*;

public interface DocumentRatesRepository extends R2dbcRepository<DocumentRates, Integer> {

    // Custom query methods can be defined here if needed
    // For example, to find by legalType and documentType
    @Query("SELECT * FROM core.document_rates " +
    "WHERE legal_type = :legalType AND document_type = :documentType AND local_type = :localType")
    Mono<DocumentRates> findByLegalTypeAndDocumentTypeAndLocalType(
        Integer legalType, Integer documentType, Integer localType);    
}
