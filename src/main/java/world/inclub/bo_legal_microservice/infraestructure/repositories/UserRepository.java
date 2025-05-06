package world.inclub.bo_legal_microservice.infraestructure.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.domain.models.User;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByCorreo(String correo);
}
