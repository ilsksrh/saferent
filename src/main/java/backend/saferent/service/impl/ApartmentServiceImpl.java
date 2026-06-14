package backend.saferent.service.impl;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.ApartmentPhoto;
import backend.saferent.entity.District;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.ApartmentMapper;
import backend.saferent.repository.ApartmentPhotoRepository;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.DistrictRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.search.ApartmentSearchService;
import backend.saferent.service.ApartmentService;
import backend.saferent.service.FileStorageService;
import backend.saferent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentPhotoRepository apartmentPhotoRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final ApartmentMapper apartmentMapper;
    private final ApartmentSearchService apartmentSearchService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    @Override
    public ApartmentResponse createApartment(CreateApartmentRequest request) {
        User landlord = userRepository.findById(request.getLandlordId())
                .orElseThrow(() -> new NotFoundException("Арендодатель не найден"));

        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new NotFoundException("Район не найден"));

        Apartment apartment = apartmentMapper.toEntity(request, landlord, district);
        apartment = apartmentRepository.save(apartment);
        apartmentSearchService.index(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public ApartmentResponse getById(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));
        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public List<ApartmentResponse> getAllActive() {
        // Публичная лента: только верифицированные
        return apartmentRepository.findAllByStatusAndVerifiedTrueAndDeletedAtIsNull(ApartmentStatus.ACTIVE)
                .stream()
                .map(apartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApartmentResponse> getByLandlord(UUID landlordId) {
        return apartmentRepository.findAllByLandlordId(landlordId)
                .stream()
                .map(apartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApartmentResponse updateApartment(UUID id, UpdateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));

        apartmentMapper.updateEntity(apartment, request);
        apartment = apartmentRepository.save(apartment);
        apartmentSearchService.index(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public void deleteApartment(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена" + id));

        apartment.setDeletedAt(LocalDateTime.now());
        apartment.setStatus(ApartmentStatus.ARCHIVED);
        apartmentRepository.save(apartment);
        apartmentSearchService.delete(id);
    }

    @Override
    public ApartmentResponse verifyApartment(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));
        boolean wasNotVerified = !apartment.isVerified();
        apartment.setVerified(true);
        apartment.setRejectionReason(null);
        apartment.setRejectedAt(null);
        Apartment saved = apartmentRepository.save(apartment);
        apartmentSearchService.index(saved);

        if (wasNotVerified && saved.getLandlord() != null) {
            notificationService.create(
                    saved.getLandlord().getId(),
                    "Объявление верифицировано",
                    "Ваше объявление \"" + saved.getTitle() + "\" прошло проверку и опубликовано.",
                    NotificationType.SYSTEM,
                    saved.getId(),
                    "Apartment"
            );
        }
        return apartmentMapper.toResponse(saved);
    }

    @Override
    public ApartmentResponse rejectApartment(UUID id, String reason) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));
        apartment.setVerified(false);
        apartment.setRejectionReason(reason);
        apartment.setRejectedAt(LocalDateTime.now());
        Apartment saved = apartmentRepository.save(apartment);
        apartmentSearchService.index(saved); // удалит из индекса (verified=false)

        if (saved.getLandlord() != null) {
            notificationService.create(
                    saved.getLandlord().getId(),
                    "Объявление отклонено",
                    "Ваше объявление \"" + saved.getTitle() + "\" отклонено модератором. Причина: " + reason,
                    NotificationType.SYSTEM,
                    saved.getId(),
                    "Apartment"
            );
        }
        return apartmentMapper.toResponse(saved);
    }

    @Override
    public List<ApartmentResponse> getPendingForModeration() {
        return apartmentRepository.findAllByVerifiedFalseAndDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(apartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApartmentResponse addPhoto(UUID apartmentId, MultipartFile file, UUID currentUserId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));

        if (!isOwnerOrAdmin(apartment, currentUserId)) {
            throw new BadRequestException("Только владелец или админ может загружать фото");
        }

        String url = fileStorageService.upload(file, "apartments/" + apartmentId);

        short nextPosition = (short) (apartmentPhotoRepository.findByApartment(apartment).size() + 1);
        ApartmentPhoto photo = ApartmentPhoto.builder()
                .apartment(apartment)
                .url(url)
                .position(nextPosition)
                .build();
        apartmentPhotoRepository.save(photo);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    @Transactional
    public void deletePhoto(UUID apartmentId, UUID photoId, UUID currentUserId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));

        if (!isOwnerOrAdmin(apartment, currentUserId)) {
            throw new BadRequestException("Только владелец или админ может удалять фото");
        }

        ApartmentPhoto photo = apartmentPhotoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Фото не найдено"));

        if (!photo.getApartment().getId().equals(apartmentId)) {
            throw new BadRequestException("Фото не принадлежит этой квартире");
        }

        apartmentPhotoRepository.delete(photo);
    }

    @Override
    @Transactional
    public ApartmentResponse addPanorama(UUID apartmentId, MultipartFile file, UUID currentUserId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));

        if (!isOwnerOrAdmin(apartment, currentUserId)) {
            throw new BadRequestException("Только владелец или админ может загружать панорамы");
        }

        String url = fileStorageService.upload(file, "apartments/" + apartmentId + "/panorama");
        apartment.getPanoramaUrls().add(url);
        apartmentRepository.save(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    @Transactional
    public ApartmentResponse removePanorama(UUID apartmentId, String url, UUID currentUserId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Квартира не найдена"));

        if (!isOwnerOrAdmin(apartment, currentUserId)) {
            throw new BadRequestException("Только владелец или админ может удалять панорамы");
        }

        apartment.getPanoramaUrls().remove(url);
        apartmentRepository.save(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    private boolean isOwnerOrAdmin(Apartment apartment, UUID currentUserId) {
        if (apartment.getLandlord() != null
                && apartment.getLandlord().getId().equals(currentUserId)) {
            return true;
        }
        return userRepository.findById(currentUserId)
                .map(backend.saferent.entity.User::isAdmin)
                .orElse(false);
    }
}
