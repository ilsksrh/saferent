package backend.saferent.entity;
import backend.saferent.entity.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends AbstractEntity {

    @ManyToOne
    @NotNull
    private Apartment apartment;

    @ManyToOne
    @NotNull
    private User tenant;

    @NotNull
    private java.time.LocalDate startDate;

    @NotNull
    private java.time.LocalDate endDate;

    private String message;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}