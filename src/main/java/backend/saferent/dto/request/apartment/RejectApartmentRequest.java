package backend.saferent.dto.request.apartment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectApartmentRequest {

    @NotBlank(message = "Причина обязательна")
    @Size(max = 1024)
    private String reason;
}
