package world.inclub.bo_legal_microservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChangeStatusRequest {
    private Integer status;
    private String  reasonText;    
    private Integer reasonType;
    private Integer userPanelId;     
    private String  documentKey;
}
