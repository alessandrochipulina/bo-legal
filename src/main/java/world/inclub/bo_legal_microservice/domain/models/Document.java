package world.inclub.bo_legal_microservice.domain.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.document")
@Builder(toBuilder = true)
public class Document {
    @Id
    private Integer id;
    private String imageUrl;
    private String documentUrl;
    private Integer status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer userPanelId;
    private Integer documentTypeId;
    private String documentTypeName;
    private String documentKey;
    private String documentTargetKey;
    private String documentVoucherKey;
    private String userId;
    private String userDate;
    private String userRealName;
    private String userDni;
    private String userLocal;
    private String userLocalName;
    private Integer userLocalType;
    private Integer legalizationType;
    private String legalizationName;
    private String portfolioName;
    private float price;
}
