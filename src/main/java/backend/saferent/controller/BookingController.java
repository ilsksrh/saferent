package backend.saferent.controller;

import backend.saferent.dto.request.booking.CreateBookingRequest;
import backend.saferent.dto.response.booking.BookingResponse;
import backend.saferent.service.BookingService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Заявки на аренду")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils  securityUtils;

    @Operation(summary = "Подать заявку на аренду")
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @Operation(summary = "Мои заявки (как арендатор)")
    @GetMapping("/my/tenant")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @Operation(summary = "Входящие заявки (как арендодатель)")
    @GetMapping("/my/landlord")
    public ResponseEntity<List<BookingResponse>> getIncoming() {
        return ResponseEntity.ok(bookingService.getIncomingBookings());
    }

    @Operation(summary = "Детали заявки")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @Operation(summary = "Одобрить заявку (арендодатель)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    @Operation(summary = "Отклонить заявку (арендодатель)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @Operation(summary = "Отменить заявку (арендатор)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}