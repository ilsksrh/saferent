package backend.saferent.repository;

import backend.saferent.entity.District;
import backend.saferent.entity.DistrictRating;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRatingRepository extends JpaRepository<DistrictRating, UUID> {

    Optional<DistrictRating> findByDistrictAndUser(District district, User user);

    @Query("SELECT COALESCE(AVG(r.safetyRating), 0) FROM DistrictRating r WHERE r.district = :district")
    Double findAverageSafetyByDistrict(@Param("district") District district);

    @Query("SELECT COALESCE(AVG(r.comfortRating), 0) FROM DistrictRating r WHERE r.district = :district")
    Double findAverageComfortByDistrict(@Param("district") District district);
}