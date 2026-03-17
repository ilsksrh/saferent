package backend.saferent.dto.request.user;

import backend.saferent.entity.enums.PreferredRole;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String name;

    private PreferredRole preferredRole;

    @Size(max = 50, message = "eGov ID не должен превышать 50 символов")
    private String egovId;

    @Size(max = 1024, message = "URL аватара слишком длинный")
    private String avatarUrl;
}