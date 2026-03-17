package backend.saferent.repository;

import backend.saferent.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID> {
    Optional<District> findByName(String name);
    boolean existsByName(String name);
}