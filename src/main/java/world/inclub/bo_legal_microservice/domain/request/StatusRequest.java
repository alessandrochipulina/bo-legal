package world.inclub.bo_legal_microservice.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {
    private String  color;    
    private String  name;        
    private String  detail;    
    private String  description;
    private Integer active;
}