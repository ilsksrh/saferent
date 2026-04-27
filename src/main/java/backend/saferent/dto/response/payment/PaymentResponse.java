package backend.saferent.dto.response.payment;

import backend.saferent.entity.enums.PaymentMethod;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;

    private UUID contractId;
    private String apartmentTitle;

    private UUID payerId;
    private String payerName;

    private PaymentType type;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;

    private String transactionId;
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}