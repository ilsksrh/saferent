package backend.saferent.dto.request.auth;

import backend.saferent.entity.enums.PreferredRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(min = 6, max = 50, message = "Password must be 6-50 characters")
    private String password;

    private PreferredRole preferredRole;
}