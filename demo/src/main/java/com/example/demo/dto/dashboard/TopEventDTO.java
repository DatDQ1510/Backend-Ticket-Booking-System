package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Top Events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEventDTO {
    private Long eventId;
    private String title;
    private BigDecimal totalRevenue;
    private Long totalBookings;
    private Long ticketsSold;
    private Integer availableSeats;
    private Double occupancyRate;
}
