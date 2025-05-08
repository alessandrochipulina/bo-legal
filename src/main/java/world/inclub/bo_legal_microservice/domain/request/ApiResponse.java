package world.inclub.bo_legal_microservice.domain.request;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status = HttpStatus.OK.toString();
    private String message;
    private T data;

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }
}