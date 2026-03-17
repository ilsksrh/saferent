package backend.saferent.repository;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.enums.ApartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID>, JpaSpecificationExecutor<Apartment> {

    List<Apartment> findAllByStatusAndDeletedAtIsNull(ApartmentStatus status);

    List<Apartment> findAllByLandlordId(UUID landlordId);

}