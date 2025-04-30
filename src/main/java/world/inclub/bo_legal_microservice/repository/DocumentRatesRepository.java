package world.inclub.bo_legal_microservice.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import world.inclub.bo_legal_microservice.model.*;


public interface DocumentRatesRepository extends R2dbcRepository<DocumentRates, Integer> {

}
