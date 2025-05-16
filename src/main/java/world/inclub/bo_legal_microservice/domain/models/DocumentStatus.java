package world.inclub.bo_legal_microservice.domain.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.document_status")
public class DocumentStatus {   
    @Id 
    private Integer id;
    @NotBlank(message = "color no puede estar vacio")
    private String color;  
    @NotBlank(message = "name no puede estar vacio")
    private String name;
    private String description;
    private String detail;
    @Max(value = 1, message = "active debe ser menor o igual a 1")
    @Min(value = 0, message = "active debe ser mayor o igual a 0")
    private Integer active;
    private Integer isDeleteable;
}
