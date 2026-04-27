package backend.saferent.dto.response.auth;

import backend.saferent.entity.enums.PreferredRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;      // "Bearer"

    private UUID userId;
    private String name;
    private String phone;
    private PreferredRole role;
    private boolean verified;
}