package backend.saferent.util;

import backend.saferent.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }
        throw new RuntimeException("Not authenticated");
    }

    public java.util.UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}