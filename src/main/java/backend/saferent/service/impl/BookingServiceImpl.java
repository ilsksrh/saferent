package backend.saferent.service.impl;

import backend.saferent.dto.request.booking.CreateBookingRequest;
import backend.saferent.dto.response.booking.BookingResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Booking;
import backend.saferent.entity.Contract;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.BookingStatus;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.BookingMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.BookingRepository;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.BookingService;
import backend.saferent.service.NotificationService;
import backend.saferent.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository    bookingRepository;
    private final ApartmentRepository  apartmentRepository;
    private final UserRepository       userRepository;
    private final ContractRepository   contractRepository;
    private final BookingMapper        bookingMapper;
    private final SecurityUtils        securityUtils;
    private final NotificationService  notificationService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Apartment apartment = apartmentRepository
                .findById(request.getApartmentId())
                .orElseThrow(() -> new NotFoundException("Apartment not found"));

        if (apartment.getDeletedAt() != null) {
            throw new BadRequestException("Apartment is deleted");
        }

        if (!apartment.isVerified()) {
            throw new BadRequestException("Apartment must be verified first");
        }

        UUID tenantId = securityUtils.getCurrentUserId();
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        if (apartment.getLandlord().getId().equals(tenantId)) {
            throw new BadRequestException(
                    "You cannot book your own apartment"
            );
        }

        boolean alreadyPending = bookingRepository
                .existsByApartmentAndTenantAndStatus(
                        apartment, tenant, BookingStatus.PENDING
                );
        if (alreadyPending) {
            throw new BadRequestException(
                    "You already have a pending booking for this apartment"
            );
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Booking booking = Booking.builder()
                .apartment(apartment)
                .tenant(tenant)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .message(request.getMessage())
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        notificationService.create(
                booking.getApartment().getLandlord().getId(),
                "New Booking Request",
                tenant.getName() + " wants to rent: " +
                        apartment.getTitle(),
                NotificationType.BOOKING,
                booking.getId(),
                "BOOKING"
        );

        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        UUID tenantId = securityUtils.getCurrentUserId();
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return bookingRepository.findByTenant(tenant)
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getIncomingBookings() {
        UUID landlordId = securityUtils.getCurrentUserId();
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return bookingRepository.findByApartment_Landlord(landlord)
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse approveBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        UUID landlordId = securityUtils.getCurrentUserId();
        checkIsLandlord(booking, landlordId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException(
                    "Cannot approve booking with status: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        createDraftContract(booking);

        notificationService.create(
                booking.getTenant().getId(),
                "Booking Approved!",
                "Your booking for " + booking.getApartment().getTitle() +
                        " has been approved. Please sign the contract.",
                NotificationType.BOOKING,
                booking.getId(),
                "BOOKING"
        );

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        UUID landlordId = securityUtils.getCurrentUserId();
        checkIsLandlord(booking, landlordId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException(
                    "Cannot reject booking with status: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        notificationService.create(
                booking.getTenant().getId(),
                "Booking Rejected",
                "Your booking for " + booking.getApartment().getTitle() +
                        " was rejected.",
                NotificationType.BOOKING,
                booking.getId(),
                "BOOKING"
        );

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        UUID tenantId = securityUtils.getCurrentUserId();

        if (!booking.getTenant().getId().equals(tenantId)) {
            throw new BadRequestException(
                    "You can only cancel your own bookings"
            );
        }

        if (booking.getStatus() == BookingStatus.APPROVED ||
                booking.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException(
                    "Cannot cancel booking with status: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }


    private Booking getBookingOrThrow(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Booking not found: " + id
                ));
    }

    private void checkIsLandlord(Booking booking, UUID landlordId) {
        if (!booking.getApartment().getLandlord().getId().equals(landlordId)) {
            throw new BadRequestException(
                    "You are not the landlord of this apartment"
            );
        }
    }

    private void createDraftContract(Booking booking) {
        Contract contract = Contract.builder()
                .tenant(booking.getTenant())
                .landlord(booking.getApartment().getLandlord())
                .apartment(booking.getApartment())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .rentAmount(booking.getApartment().getPrice())
                .depositAmount(booking.getApartment().getDepositAmount() != null
                        ? booking.getApartment().getDepositAmount()
                        : booking.getApartment().getPrice())
                .status(ContractStatus.DRAFT)
                .terms("Standard SafeRent rental agreement.")
                .build();

        contractRepository.save(contract);
    }
}