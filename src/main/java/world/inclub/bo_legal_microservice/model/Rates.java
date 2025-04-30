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
@Table("core.rates")
public class Rates {
    @Id
    private Integer id;
    private Integer legalizationType;
    private Integer documentTypeId;
    private Integer localType;
    private Float price;
    private Integer userPanelId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
