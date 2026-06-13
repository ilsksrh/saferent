package backend.saferent.service;

import java.util.UUID;

public interface OtpService {

    void sendOtp(UUID userId, String email);

    boolean verifyOtp(UUID userId, String code);
}
