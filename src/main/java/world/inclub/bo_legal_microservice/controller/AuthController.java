package world.inclub.bo_legal_microservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;
import world.inclub.bo_legal_microservice.application.services.UserService;
import world.inclub.bo_legal_microservice.domain.models.User;
import world.inclub.bo_legal_microservice.domain.request.LoginRequest;
import world.inclub.bo_legal_microservice.domain.request.TokenResponse;
import world.inclub.bo_legal_microservice.security.JwtUtil;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody LoginRequest login) {
        return usuarioService.autenticar(login.getCorreo(), login.getContrasena())
            .map(usuario -> {
                String token = jwtUtil.generarToken(usuario.getCorreo());
                return ResponseEntity.ok(new TokenResponse(token));
            })
            // .switchIfEmpty(Mono.just(ResponseEntity.status(401).body("Credenciales inválidas")));
            .defaultIfEmpty(ResponseEntity.status(401).body(new TokenResponse("")))
            .onErrorResume(_ -> {
                return Mono.just(ResponseEntity.status(401).body(new TokenResponse("")));
            });
    }

     // Endpoint para registrar un nuevo usuario
    @PostMapping("/user")
    public Mono<User> crearUsuario(@RequestBody Mono<User> usuarioMono) {
        return usuarioMono
                .flatMap(usuarioService::crear)
                .map(usuario -> {
                    // Para evitar retornar la contraseña al cliente
                    usuario.setContrasena(null);
                    return usuario;
                });
    }
}
