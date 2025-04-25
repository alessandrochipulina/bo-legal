package world.inclub.bo_legal_microservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppProperties {
    private Status status;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private Integer pendiente;
        private Integer aprobado;
        private Integer rechazado;
        private Integer atendido;
        private Integer recojo;
        private Integer proceso;
        private Integer custom;
    }    
}