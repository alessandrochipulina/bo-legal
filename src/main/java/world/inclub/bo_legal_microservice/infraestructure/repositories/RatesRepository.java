package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.Rates;

public interface RatesRepository extends R2dbcRepository<Rates, Integer> {
    // Custom query methods can be defined here if needed
    // For example, to find by legalType and documentType
    @Query("SELECT * FROM core.rates " +
    "WHERE legalization_type = :legalType AND document_type_id = :documentType AND local_type = :localType")
    Mono<Rates> findByLegalTypeAndDocumentTypeAndLocalType(
        Integer legalType, Integer documentType, Integer localType);    
}
