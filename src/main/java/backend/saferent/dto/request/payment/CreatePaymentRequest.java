package backend.saferent.dto.request.payment;

import backend.saferent.entity.enums.PaymentMethod;
import backend.saferent.entity.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;
    
    @NotNull(message = "Payment type is required")
    private PaymentType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}