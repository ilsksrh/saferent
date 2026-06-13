package backend.saferent.controller;

import backend.saferent.dto.response.user.UserResponse;
import backend.saferent.entity.User;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.UserMapper;
import backend.saferent.repository.UserRepository;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Эндпоинты для модератора")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Список пользователей (paged)")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByNameOrPhoneContaining(search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return ResponseEntity.ok(users.map(userMapper::toResponse));
    }

    @Operation(summary = "Назначить/снять админа")
    @PatchMapping("/users/{id}/admin")
    public ResponseEntity<UserResponse> setAdmin(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {

        UUID currentId = securityUtils.getCurrentUserId();
        if (currentId.equals(id)) {
            return ResponseEntity.badRequest().build(); // нельзя снять админа с себя
        }

        boolean makeAdmin = Boolean.TRUE.equals(body.get("admin"));
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setAdmin(makeAdmin);
        return ResponseEntity.ok(userMapper.toResponse(userRepository.save(user)));
    }
}
