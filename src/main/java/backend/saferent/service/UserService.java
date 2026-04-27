package backend.saferent.service;

import backend.saferent.dto.request.user.CreateUserRequest;
import backend.saferent.dto.request.user.UpdateUserRequest;
import backend.saferent.dto.response.user.UserResponse;
import backend.saferent.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    Optional<UserResponse> findById(UUID id);

    Optional<UserResponse> findByPhone(String phone);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void verifyUser(UUID id);

    void updateLastLogin(UUID id);

    User getUserEntityOrThrow(UUID id);

    boolean existsByPhone(String phone);
}