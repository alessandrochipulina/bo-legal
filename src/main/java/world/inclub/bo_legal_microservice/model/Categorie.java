package world.inclub.bo_legal_microservice.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.categorie")
public class Categorie {
    @Id
    private Integer id;
    private String  categorieName;
    private Integer categorieId;
    private String categorieItemName;
    private Integer categorieItemId;    
    private Integer userPanelId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}