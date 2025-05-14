package world.inclub.bo_legal_microservice.domain.models;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("core.ciclo")
@Builder(toBuilder = true)
public class Ciclo {
    @Id
    private Integer id;

    private Integer legalizationType;

    private String legalizationName;
    
    private String name;

    private Integer status;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer allDay;

    private LocalTime startHourAt;
    private LocalTime endHourAt;
    private String locale;
    private String description;    
    private String color;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer userPanelId;
}