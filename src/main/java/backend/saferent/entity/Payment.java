package backend.saferent.entity;
import backend.saferent.entity.enums.PaymentMethod;
import backend.saferent.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends AbstractEntity {

    @ManyToOne
    private Contract contract;

    @DecimalMin("0.0")
    private java.math.BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    private java.time.LocalDateTime paidAt;
}