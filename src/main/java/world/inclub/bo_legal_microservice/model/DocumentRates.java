package world.inclub.bo_legal_microservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.document_rates")
public class DocumentRates {
    @Id
    private Integer id;
    private Integer legalType;
    private String legalName;
    private Integer documentType;
    private String documentName;    
    private Integer localType;
    private String localName;
    private Float price;
}