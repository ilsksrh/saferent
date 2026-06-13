package backend.saferent.service.impl;

import backend.saferent.dto.request.auth.LoginRequest;
import backend.saferent.dto.request.auth.RegisterRequest;
import backend.saferent.dto.response.auth.AuthResponse;
import backend.saferent.entity.User;
import backend.saferent.exception.BadRequestException;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.AuthService;
import backend.saferent.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException(
                    "User with this phone already exists"
            );
        }

        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .preferredRole(request.getPreferredRole())
                .verified(false)
                .build();

        user = userRepository.save(user);
        return toAuthResponse(user, jwtUtil.generateToken(user.getId(), user.getPhone()));
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid phone or password"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid phone or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return toAuthResponse(user, jwtUtil.generateToken(user.getId(), user.getPhone()));
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getPreferredRole())
                .verified(user.isVerified())
                .admin(user.isAdmin())
                .build();
    }
}