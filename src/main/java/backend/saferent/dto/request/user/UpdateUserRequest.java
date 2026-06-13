package backend.saferent.dto.request.user;

import backend.saferent.entity.enums.PreferredRole;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String name;

    @Size(max = 100)
    private String email;

    private PreferredRole preferredRole;

    @Size(max = 50, message = "eGov ID не должен превышать 50 символов")
    private String egovId;

    @Size(max = 1024, message = "URL аватара слишком длинный")
    private String avatarUrl;

    @Size(max = 60)
    private String city;

    @Size(max = 120)
    private String languages;

    @Size(max = 500)
    private String bio;
}