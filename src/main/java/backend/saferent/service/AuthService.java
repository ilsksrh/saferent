package backend.saferent.service;

import backend.saferent.dto.request.auth.LoginRequest;
import backend.saferent.dto.request.auth.RegisterRequest;
import backend.saferent.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}