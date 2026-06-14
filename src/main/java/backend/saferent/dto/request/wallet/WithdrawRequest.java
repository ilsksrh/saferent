package backend.saferent.dto.request.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {

    @NotNull
    @DecimalMin(value = "1.0", message = "Сумма вывода должна быть положительной")
    private BigDecimal amount;
}
