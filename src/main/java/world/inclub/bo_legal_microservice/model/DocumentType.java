package world.inclub.bo_legal_microservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.document_type")
public class DocumentType {
    @Id
    private Integer id;
    private String description;    
}
