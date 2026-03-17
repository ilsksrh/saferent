package backend.saferent.repository;

import backend.saferent.entity.Apartment;
import backend.saferent.entity.ApartmentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentPhotoRepository extends JpaRepository<ApartmentPhoto, UUID> {

    List<ApartmentPhoto> findByApartment(Apartment apartment);

}