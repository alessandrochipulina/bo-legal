package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import world.inclub.bo_legal_microservice.domain.models.DocumentType;

public interface DocumentTypeRepository extends R2dbcRepository<DocumentType, Integer> {
}
