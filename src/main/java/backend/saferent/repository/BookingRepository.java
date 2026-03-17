package backend.saferent.repository;

import backend.saferent.entity.Booking;
import backend.saferent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByTenant(User tenant);

}