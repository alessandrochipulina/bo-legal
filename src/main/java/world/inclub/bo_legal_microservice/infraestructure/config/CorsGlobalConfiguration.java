package world.inclub.bo_legal_microservice.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsGlobalConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://gateway-dev.inclub:8090");
        config.addAllowedOrigin("https://bo-admin-2.web.app");
        config.addAllowedOrigin("https://gateway.inclub.world");
        config.addAllowedOrigin("https://inclub.world");
        config.addAllowedOrigin("https://www.inclub.world");
        config.addAllowedOrigin("https://membershipapi.inclub.world");
        config.addAllowedOrigin("https://panel.inclub.world");
        config.addAllowedOrigin("http://localhost:4200/*");
        config.addAllowedOrigin("http://localhost:4200");        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}