package backend.saferent.entity;

import backend.saferent.entity.enums.PreferredRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {

    @NotBlank
    @Size(max = 20)
    @Column(unique = true, nullable = false)
    private String phone;

    @Size(max = 100)
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private PreferredRole preferredRole;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String egovId;

    @Column(nullable = false)
    private boolean verified = false;

    @Size(max = 1024)
    private String avatarUrl;

    private LocalDateTime lastLoginAt;

    @Size(max = 50)
    private String loginProvider;

    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Apartment> apartments = new ArrayList<>();

    public boolean isLandlord() {
        return preferredRole == PreferredRole.LANDLORD;
    }

    @Column
    private String passwordHash;
}