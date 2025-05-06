package world.inclub.bo_legal_microservice.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    private Integer status;
    private String  reasonText;    
    private Integer reasonType;
    private Integer userPanelId;     
    private String  documentKey;
    private String  userId;
    private Integer categorieId;
}
