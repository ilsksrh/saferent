package backend.saferent.dto.request.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {

    @NotNull
    @DecimalMin(value = "1.0", message = "Сумма пополнения должна быть положительной")
    private BigDecimal amount;

    private String method; // KASPI / CARD (mock)
}
