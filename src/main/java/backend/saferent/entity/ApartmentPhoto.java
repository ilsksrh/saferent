package backend.saferent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentPhoto extends AbstractEntity {

    @ManyToOne
    @NotNull
    private Apartment apartment;

    @NotBlank
    @Size(max = 1024)
    private String url;

    private Short position;
}