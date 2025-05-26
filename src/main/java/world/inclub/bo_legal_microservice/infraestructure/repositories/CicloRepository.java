package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import world.inclub.bo_legal_microservice.domain.models.*;

public interface CicloRepository extends R2dbcRepository<Ciclo, Integer> {

}

