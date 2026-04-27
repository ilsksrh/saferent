package backend.saferent.service.impl;

import backend.saferent.dto.response.user.LandlordProfileResponse;
import backend.saferent.dto.response.user.LandlordApartmentSummary;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.PreferredRole;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.LandlordMapper;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.LandlordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandlordServiceImpl implements LandlordService {

    private final UserRepository userRepository;
    private final LandlordMapper landlordMapper;

    @Override
    public LandlordProfileResponse getLandlordProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!user.isLandlord()) {
            throw new IllegalStateException("Пользователь не является арендодателем");
        }

        return landlordMapper.toProfileResponse(user);
    }

    @Override
    public List<LandlordApartmentSummary> getLandlordApartments(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return user.getApartments().stream()
                .map(landlordMapper::toApartmentSummary)
                .collect(Collectors.toList());
    }

    @Override
    public void setUserAsLandlord(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (user.getPreferredRole() == PreferredRole.LANDLORD) {
            throw new IllegalStateException("Пользователь уже арендодатель");
        }

        user.setPreferredRole(PreferredRole.LANDLORD);
        userRepository.save(user);
    }
}