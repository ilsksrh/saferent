package backend.saferent.dto.request.apartment;

import backend.saferent.entity.enums.ApartmentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateApartmentRequest {

    // landlordId берётся из JWT текущего пользователя — не из тела запроса
    private UUID landlordId;

    @NotNull(message = "ID района обязателен")
    private UUID districtId;

    @NotBlank(message = "Название обязательно")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotBlank(message = "Адрес обязателен")
    @Size(max = 255)
    private String address;

    @NotNull(message = "Цена обязательна")
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull(message = "Площадь обязательна")
    @DecimalMin("0.0")
    private BigDecimal area;

    @Min(value = 1, message = "Количество комнат минимум 1")
    private Short rooms;

    private LocalDate availableFrom;

    private Boolean verified;

    private ApartmentStatus status;
}