package backend.saferent.dto.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateChatRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;

    @NotNull(message = "Apartment ID is required")
    private UUID apartmentId;
}