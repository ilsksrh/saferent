package backend.saferent.dto.request.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateBookingRequest {

    @NotNull(message = "ID квартиры обязателен")
    private UUID apartmentId;

    @NotNull(message = "Дата заезда обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата выезда обязательна")
    private LocalDate endDate;

    private String message;
}