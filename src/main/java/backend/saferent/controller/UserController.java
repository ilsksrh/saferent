package backend.saferent.controller;

import backend.saferent.dto.request.user.CreateUserRequest;
import backend.saferent.dto.request.user.UpdateUserRequest;
import backend.saferent.dto.response.user.UserResponse;
import backend.saferent.service.UserService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Управление пользователями")
@RequiredArgsConstructor
public class UserController {

    private final UserService   userService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Получить текущего пользователя из токена")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        UUID userId = securityUtils.getCurrentUserId();
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Получить пользователя по номеру телефона")
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserResponse> getByPhone(@PathVariable String phone) {
        return userService.findByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Обновить профиль пользователя")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Запросить OTP-код для верификации (отправляется на email)")
    @PostMapping("/{id}/verify/request")
    public ResponseEntity<Map<String, String>> requestOtp(@PathVariable UUID id) {
        userService.requestOtp(id);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email address"));
    }

    @Operation(summary = "Верифицировать пользователя по OTP-коду из email")
    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, String>> verify(
            @PathVariable UUID id,
            @RequestParam String code) {
        userService.verifyUser(id, code);
        return ResponseEntity.ok(Map.of("message", "User verified successfully"));
    }

    @Operation(summary = "Обновить время последнего входа")
    @PostMapping("/{id}/login")
    public ResponseEntity<Map<String, String>> updateLastLogin(@PathVariable UUID id) {
        userService.updateLastLogin(id);
        return ResponseEntity.ok(Map.of("message", "Last login updated"));
    }

    @Operation(summary = "Загрузить аватар (multipart → MinIO)")
    @PostMapping(value = "/{id}/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadAvatar(@PathVariable UUID id,
                                                     @RequestParam("file") MultipartFile file) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.uploadAvatar(id, file, currentUserId));
    }
}
