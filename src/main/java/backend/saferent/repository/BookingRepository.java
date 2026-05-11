package backend.saferent.repository;

import backend.saferent.entity.Booking;
import backend.saferent.entity.User;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByTenant(User tenant);

    List<Booking> findByApartment_Landlord(User landlord);

    List<Booking> findByApartment(Apartment apartment);

    boolean existsByApartmentAndTenantAndStatus(
            Apartment apartment, User tenant, BookingStatus status
    );
}