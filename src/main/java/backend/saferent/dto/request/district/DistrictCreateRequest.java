package backend.saferent.dto.request.district;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DistrictCreateRequest {

    @NotBlank(message = "Название района обязательно")
    @Size(max = 50)
    private String name;

    @Min(value = 0, message = "Оценка безопасности от 0 до 10")
    @Max(value = 10)
    private Integer safetyScore = 0;

    @Min(value = 0, message = "Оценка комфорта от 0 до 10")
    @Max(value = 10)
    private Integer comfortScore = 0;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Цвет должен быть в формате HEX (#RRGGBB)")
    @NotBlank(message = "Цвет обязателен")
    private String colorCode;
}