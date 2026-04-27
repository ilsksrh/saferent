package backend.saferent.service.impl;

import backend.saferent.dto.request.district.DistrictCreateRequest;
import backend.saferent.dto.request.district.DistrictRatingRequest;
import backend.saferent.dto.response.district.DistrictResponse;
import backend.saferent.entity.District;
import backend.saferent.entity.DistrictRating;
import backend.saferent.entity.User;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.DistrictMapper;
import backend.saferent.repository.DistrictRatingRepository;
import backend.saferent.repository.DistrictRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictRatingRepository districtRatingRepository;
    private final UserRepository userRepository;
    private final DistrictMapper districtMapper;


    @Override
    public DistrictResponse createDistrict(DistrictCreateRequest request) {
        if (districtRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Район с таким названием уже существует");
        }

        District district = districtMapper.toEntity(request);
        district = districtRepository.save(district);

        return districtMapper.toResponse(district);
    }
    @Override
    public List<DistrictResponse> getAllDistricts() {
        return districtRepository.findAll()
                .stream()
                .map(districtMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DistrictResponse getById(UUID id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Район не найден"));
        return districtMapper.toResponse(district);
    }

    @Override
    public void rateDistrict(DistrictRatingRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new NotFoundException("Район не найден"));

        DistrictRating existing = districtRatingRepository.findByDistrictAndUser(district, user)
                .orElse(null);

        if (existing != null) {
            existing.setSafetyRating(request.getSafetyRating());
            existing.setComfortRating(request.getComfortRating());
            districtRatingRepository.save(existing);
        } else {
            DistrictRating rating = DistrictRating.builder()
                    .district(district)
                    .user(user)
                    .safetyRating(request.getSafetyRating())
                    .comfortRating(request.getComfortRating())
                    .build();
            districtRatingRepository.save(rating);
        }

    }
}