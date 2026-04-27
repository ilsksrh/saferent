package backend.saferent.service.impl;

import backend.saferent.dto.request.user.CreateUserRequest;
import backend.saferent.dto.request.user.UpdateUserRequest;
import backend.saferent.dto.response.user.UserResponse;
import backend.saferent.entity.User;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.UserMapper;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Such user already exists");
        }

        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse);
    }

    @Override
    public Optional<UserResponse> findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .map(userMapper::toResponse);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = getUserEntityOrThrow(id);
        userMapper.updateEntity(user, request);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public void verifyUser(UUID id) {
        User user = getUserEntityOrThrow(id);
        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public void updateLastLogin(UUID id) {
        User user = getUserEntityOrThrow(id);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User getUserEntityOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("User not found: " + id));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}