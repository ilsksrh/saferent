package backend.saferent.mapper;

import backend.saferent.dto.request.user.CreateUserRequest;
import backend.saferent.dto.request.user.UpdateUserRequest;
import backend.saferent.dto.response.user.UserResponse;
import backend.saferent.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .name(request.getName())
                .preferredRole(request.getPreferredRole())
                .egovId(request.getEgovId())
                .avatarUrl(request.getAvatarUrl())
                .verified(false)
                .build();
    }

    public User updateEntity(User user, UpdateUserRequest request) {
        if (request.getName() != null)         user.setName(request.getName());
        if (request.getEmail() != null)        user.setEmail(request.getEmail());
        if (request.getPreferredRole() != null) user.setPreferredRole(request.getPreferredRole());
        if (request.getEgovId() != null)       user.setEgovId(request.getEgovId());
        if (request.getAvatarUrl() != null)    user.setAvatarUrl(request.getAvatarUrl());
        return user;
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .preferredRole(user.getPreferredRole())
                .egovId(user.getEgovId())
                .verified(user.isVerified())
                .admin(user.isAdmin())
                .avatarUrl(user.getAvatarUrl())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
