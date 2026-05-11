package backend.saferent.entity;

import backend.saferent.entity.enums.PaymentMethod;
import backend.saferent.entity.enums.PaymentStatus;
import backend.saferent.entity.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends AbstractEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionId;

    private LocalDateTime paidAt;
}