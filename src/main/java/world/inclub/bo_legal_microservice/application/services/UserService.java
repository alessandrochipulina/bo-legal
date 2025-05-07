package world.inclub.bo_legal_microservice.application.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.infraestructure.repositories.UserRepository;
import world.inclub.bo_legal_microservice.domain.models.User;    
import jakarta.validation.ConstraintViolationException;

@Service
public class UserService {

    private final UserRepository repo;
    private final Validator validator;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo, Validator validator) {
        this.repo = repo;
        this.validator = validator;
    }

    public Mono<User> crear(User usuario) {
        var errores = validator.validate(usuario);
        if (!errores.isEmpty()) throw new ConstraintViolationException(errores);

        usuario.setContrasena(encoder.encode(usuario.getContrasena()));
        return repo.save(usuario);
    }

    public Mono<User> autenticar(String correo, String contrasena) {
        return repo.findByCorreo(correo)
            .filter(u -> encoder.matches(contrasena, u.getContrasena()));
    }
}
