package backend.saferent.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordSearchResponse {

    private UUID id;
    private String name;
    private String phone;
    private String avatarUrl;
    private boolean verified;
}
