package world.inclub.bo_legal_microservice.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.lang.NonNull;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    @NonNull
    public Mono<Void> filter(
        @NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) 
    {
        String path = exchange.getRequest().getPath().toString();

        if (path.startsWith("/api/v1/document/")) {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = authHeader.substring(7);
                String correo = jwtUtil.validarToken(token);
                exchange.getAttributes().put("usuarioCorreo", correo);
            } catch (Exception e) {
                // Si el token no es válido, se devuelve un error 401 Unauthorized
                // y se detiene la cadena de filtros                
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);                
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}