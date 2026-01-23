package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Recent Activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private Long id;
    private String type;  // "user_registration", "event_created", "booking_created", etc.
    private String message;
    private LocalDateTime timestamp;
    private Long userId;
    private Long eventId;
    private Long bookingId;
}
