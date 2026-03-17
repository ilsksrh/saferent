package backend.saferent.dto.response.user;

import backend.saferent.entity.enums.PreferredRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordProfileResponse {

    private UUID id;
    private String phone;
    private String name;
    private PreferredRole preferredRole;
    private String egovId;
    private boolean verified;
    private String avatarUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Специфично для арендодателя
    private Integer totalApartments;
    private List<UUID> apartmentIds;
}