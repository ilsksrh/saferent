package backend.saferent.repository;

import backend.saferent.entity.District;
import backend.saferent.entity.DistrictRating;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRatingRepository extends JpaRepository<DistrictRating, UUID> {

    Optional<DistrictRating> findByDistrictAndUser(District district, User user);
}