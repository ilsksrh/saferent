package backend.saferent.service.impl;

import backend.saferent.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final JavaMailSender mailSender;

    private record OtpEntry(String code, LocalDateTime expiresAt) {}

    private final ConcurrentHashMap<UUID, OtpEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public void sendOtp(UUID userId, String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(userId, new OtpEntry(code, LocalDateTime.now().plusMinutes(10)));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("SafeRent — Код подтверждения");
            message.setText("""
                    Ваш код подтверждения SafeRent: %s

                    Код действителен 10 минут.
                    Если вы не запрашивали код — проигнорируйте это письмо.
                    """.formatted(code));
            mailSender.send(message);
            log.info("OTP sent to {} for user {}", email, userId);
        } catch (Exception e) {
            store.remove(userId);
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOtp(UUID userId, String code) {
        OtpEntry entry = store.get(userId);
        if (entry == null || LocalDateTime.now().isAfter(entry.expiresAt())) {
            store.remove(userId);
            return false;
        }
        if (!entry.code().equals(code)) {
            return false;
        }
        store.remove(userId);
        return true;
    }
}
