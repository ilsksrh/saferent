package backend.saferent.service;

import backend.saferent.dto.request.booking.CreateBookingRequest;
import backend.saferent.dto.response.booking.BookingResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    List<BookingResponse> getMyBookings();
    List<BookingResponse> getIncomingBookings();
    BookingResponse getById(UUID id);
    BookingResponse approveBooking(UUID bookingId);
    BookingResponse rejectBooking(UUID bookingId);
    BookingResponse cancelBooking(UUID bookingId);
}