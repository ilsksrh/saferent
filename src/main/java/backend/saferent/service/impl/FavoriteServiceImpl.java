package backend.saferent.service.impl;

import backend.saferent.dto.response.favorite.FavoriteResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.Favorite;
import backend.saferent.entity.User;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.FavoriteMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.FavoriteRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository  favoriteRepository;
    private final UserRepository      userRepository;
    private final ApartmentRepository apartmentRepository;
    private final FavoriteMapper      favoriteMapper;


    @Override
    @Transactional
    public String toggleFavorite(UUID userId, UUID apartmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));

        if (apartment.getLandlord().getId().equals(userId)) {
            throw new BadRequestException(
                    "You cannot add your own apartment to favorites"
            );
        }

        Optional<Favorite> existing =
                favoriteRepository.findByUserAndApartment(user, apartment);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return "REMOVED";
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .apartment(apartment)
                    .build();
            favoriteRepository.save(favorite);
            return "ADDED";
        }
    }


    @Override
    public List<FavoriteResponse> getMyFavorites(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(favoriteMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public boolean isFavorite(UUID userId, UUID apartmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new NotFoundException("Apartment not found"));

        return favoriteRepository.existsByUserAndApartment(user, apartment);
    }
}