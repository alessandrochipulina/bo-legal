package world.inclub.bo_legal_microservice.domain.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {
    private Integer id;
    private String  color;    
    private String  name;        
    private String  detail;    
    @Max(value = 1, message = "active debe ser menor o igual a 1")
    @Min(value = 0, message = "active debe ser mayor o igual a 0")
    private Integer active;
}