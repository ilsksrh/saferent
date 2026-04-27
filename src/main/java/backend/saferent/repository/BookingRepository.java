package backend.saferent.repository;

import backend.saferent.entity.Booking;
import backend.saferent.entity.User;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Все заявки арендатора
    List<Booking> findByTenant(User tenant);

    // Входящие заявки арендодателя (через квартиры)
    List<Booking> findByApartment_Landlord(User landlord);

    // Заявки по конкретной квартире
    List<Booking> findByApartment(Apartment apartment);

    // Активные заявки по квартире (чтобы не дублировать)
    boolean existsByApartmentAndTenantAndStatus(
            Apartment apartment, User tenant, BookingStatus status
    );
}