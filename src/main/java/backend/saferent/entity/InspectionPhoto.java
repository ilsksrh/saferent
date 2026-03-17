package backend.saferent.entity;

import backend.saferent.entity.enums.InspectionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPhoto extends AbstractEntity {

    @ManyToOne
    private Contract contract;

    @Enumerated(EnumType.STRING)
    private InspectionType type;

    @NotBlank
    @Size(max = 1024)
    private String url;
}