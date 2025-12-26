package com.example.demo.context;

import com.example.demo.custom.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * UserContext - Utility để lấy thông tin user hiện tại từ SecurityContext
 * 
 * ⭐ CÁCH SỬ DỤNG:
 * 
 * 1. Lấy userId (most common):
 *    Long userId = UserContext.getCurrentUserId();
 * 
 * 2. Lấy email:
 *    String email = UserContext.getCurrentUserEmail();
 * 
 * 3. Lấy role:
 *    String role = UserContext.getCurrentUserRole();
 * 
 * 4. Lấy full UserDetails:
 *    CustomUserDetails user = UserContext.getCurrentUser();
 * 
 * 5. Check authenticated:
 *    if (UserContext.isAuthenticated()) { ... }
 * 
 * ⭐ VÍ DỤ TRONG CONTROLLER:
 * 
 * @GetMapping("/my-bookings")
 * public List<Booking> getMyBookings() {
 *     Long userId = UserContext.getCurrentUserId();
 *     return bookingService.getBookingsByUserId(userId);
 * }
 * 
 * @PostMapping("/events/{eventId}/book")
 * public Booking bookEvent(@PathVariable Long eventId) {
 *     Long userId = UserContext.getCurrentUserId();
 *     return bookingService.createBooking(eventId, userId);
 * }
 */
@Slf4j
public class UserContext {

    private UserContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get current authenticated user's ID
     * This is the MOST USED method - get userId quickly!
     * 
     * @return userId or null if not authenticated
     */
    public static Long getCurrentUserId() {
        return getCurrentUserOptional()
                .map(CustomUserDetails::getUserId)
                .orElse(null);
    }

    /**
     * Get current authenticated user's ID (required)
     * Throws exception if user is not authenticated
     * 
     * @return userId
     * @throws IllegalStateException if user is not authenticated
     */
    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return userId;
    }

    /**
     * Get current authenticated user's email
     * 
     * @return email or null if not authenticated
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserOptional()
                .map(CustomUserDetails::getUsername)
                .orElse(null);
    }

    /**
     * Get current authenticated user's role
     * 
     * @return role or null if not authenticated
     */
    public static String getCurrentUserRole() {
        return getCurrentUserOptional()
                .map(user -> user.getAuthorities().iterator().next().getAuthority())
                .orElse(null);
    }

    /**
     * Get current authenticated user details
     * 
     * @return CustomUserDetails or null if not authenticated
     */
    public static CustomUserDetails getCurrentUser() {
        return getCurrentUserOptional().orElse(null);
    }

    /**
     * Get current authenticated user details as Optional
     * 
     * @return Optional of CustomUserDetails
     */
    public static Optional<CustomUserDetails> getCurrentUserOptional() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info(">>> Auth: {}", authentication);
            log.info(">>> Principal: {}", authentication != null ? authentication.getPrincipal() : "null");
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                return Optional.of((CustomUserDetails) principal);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error getting current user from security context", e);
            return Optional.empty();
        }
    }

    /**
     * Check if user is authenticated
     * 
     * @return true if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof CustomUserDetails;
    }

    /**
     * Check if current user has specific role
     * 
     * @param role role to check
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserOptional()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(role)))
                .orElse(false);
    }

    /**
     * Clear security context
     * Useful for testing or manual logout
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
