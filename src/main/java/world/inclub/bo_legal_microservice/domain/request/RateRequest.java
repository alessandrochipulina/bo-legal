package world.inclub.bo_legal_microservice.domain.request;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.rate_request")
public class RateRequest {
    @Id
    private Integer id;
    @Min(value = 1, message = "legalType debe ser mayor o igual a 1")
    private Integer legalType;    
    @Min(value = 1, message = "documentType debe ser mayor o igual a 1")
    private Integer documentType;    
    @Min(value = 1, message = "localType debe ser mayor o igual a 1")
    private Integer localType;    
    @Min(value = 1, message = "price debe ser mayor o igual a 1")
    private Float price;
}