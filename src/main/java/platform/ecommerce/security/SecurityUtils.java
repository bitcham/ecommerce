package platform.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import platform.ecommerce.exception.AuthenticationException;
import platform.ecommerce.exception.ErrorCode;

/**
 * Security utility methods for accessing authenticated user information.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Get current authenticated member ID from security context.
     * @return the authenticated member's ID
     * @throws AuthenticationException if not authenticated
     */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
            }
        }

        throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * Check if current user is authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Check if current user has specific role.
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }
}
