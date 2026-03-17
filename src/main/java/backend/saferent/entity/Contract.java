package backend.saferent.entity;
import backend.saferent.entity.enums.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract extends AbstractEntity {

    @ManyToOne
    private User tenant;

    @ManyToOne
    private User landlord;

    @ManyToOne
    private Apartment apartment;

    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;

    @DecimalMin("0.0")
    private java.math.BigDecimal rentAmount;

    @DecimalMin("0.0")
    private java.math.BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    private String statusReason;

    private String terms;

    private String eSignatureTenant;
    private String eSignatureLandlord;

    private java.time.LocalDateTime signedAt;
}
