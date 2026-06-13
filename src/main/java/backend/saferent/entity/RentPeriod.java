package backend.saferent.entity;

import backend.saferent.entity.enums.RentPeriodStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentPeriod extends AbstractEntity {

    @ManyToOne(optional = false)
    private Contract contract;

    private int periodIndex;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RentPeriodStatus status;

    @ManyToOne
    private Payment payment;

    private LocalDateTime paidAt;

    private LocalDateTime reminderSentAt;
}
