package world.inclub.bo_legal_microservice.domain.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.document_history")
public class DocumentHistory {
    @Id
    private Integer id;
    private String documentKey;
    private Integer status;
    private Integer reasonType;
    private String reasonText;
    private Integer userPanelId;
    private LocalDateTime createdAt;
}
