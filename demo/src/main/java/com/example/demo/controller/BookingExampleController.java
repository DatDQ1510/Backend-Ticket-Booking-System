package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.payload.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * EXAMPLE: How to use UserContext in a real Ticket Booking scenario
 * 
 * ⭐ This demonstrates practical usage of UserContext.getCurrentUserId()
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingExampleController {

    /**
     * ⭐ EXAMPLE 1: Get user's bookings
     * No need to pass userId - automatically extracted from token!
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<String>> getMyBookings() {
        // ⭐ Simply get userId from context!
        Long userId = UserContext.getCurrentUserId();
        
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }

        // TODO: Implement bookingService.getBookingsByUserId(userId)
        String message = String.format("Getting bookings for userId: %d", userId);
        
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * ⭐ EXAMPLE 2: Create booking
     * Automatically uses authenticated user's ID
     */
    @PostMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<String>> bookEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) Integer quantity) {
        
        // ⭐ Get userId from context
        Long userId = UserContext.requireCurrentUserId(); // Throws if not authenticated
        
        int qty = quantity != null ? quantity : 1;

        // TODO: Implement bookingService.createBooking(eventId, userId, qty)
        String message = String.format("User %d booked event %d (quantity: %d)", 
                userId, eventId, qty);
        
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * ⭐ EXAMPLE 3: Cancel booking
     * Verify user owns the booking before canceling
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long bookingId) {
        // ⭐ Get current user
        Long userId = UserContext.requireCurrentUserId();
        
        // TODO: Check if booking belongs to user
        // Booking booking = bookingService.getBooking(bookingId);
        // if (!booking.getUserId().equals(userId)) {
        //     throw new ForbiddenException("You can only cancel your own bookings");
        // }
        
        String message = String.format("User %d cancelled booking %d", userId, bookingId);
        
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * ⭐ EXAMPLE 4: Get booking history with filters
     * Shows how to combine UserContext with other parameters
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<String>> getBookingHistory(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        
        Long userId = UserContext.getCurrentUserId();
        String email = UserContext.getCurrentUserEmail();
        
        String message = String.format(
                "Getting booking history for user %d (%s) with status: %s, page: %d", 
                userId, email, status, pageable.getPageNumber());
        
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * ⭐ EXAMPLE 5: Admin only - Get all bookings
     * Shows role checking with UserContext
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<String>> getAllBookings(Pageable pageable) {
        // ⭐ Check if user is admin
        if (!UserContext.hasRole("ROLE_ADMIN")) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Only admins can view all bookings"));
        }
        
        Long adminId = UserContext.getCurrentUserId();
        String message = String.format("Admin %d viewing all bookings", adminId);
        
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * ⭐ EXAMPLE 6: Get available events (no authentication required)
     * But if authenticated, can show personalized info
     */
    @GetMapping("/events/available")
    public ResponseEntity<ApiResponse<String>> getAvailableEvents(Pageable pageable) {
        // TODO: Implement eventService.getAllEvents(pageable)
        // Page<EventResponse> events = eventService.getAllEvents(pageable);
        // ⭐ Optional: Add user-specific info if authenticated
        if (UserContext.isAuthenticated()) {
            Long userId = UserContext.getCurrentUserId();
            String message = String.format("Getting available events for user %d", userId);
            return ResponseEntity.ok(ApiResponse.success(message));
        }
        return ResponseEntity.ok(ApiResponse.success("Getting available events (guest)"));
    }
}
