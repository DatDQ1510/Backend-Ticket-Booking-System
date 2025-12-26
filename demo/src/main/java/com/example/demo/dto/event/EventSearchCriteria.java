package com.example.demo.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Search criteria for filtering events
 * All fields are optional - can be used via query params
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchCriteria {
    private String keyword;      // Search in title, description
    private String location;     // Filter by location
    private String eventType;    // Filter by event type (CONCERT, CONFERENCE, etc.)
    private String eventStatus;  // Filter by status (ACTIVE, CANCELLED, etc.)
    private LocalDate startDate; // Filter events from this date
    private LocalDate endDate;   // Filter events until this date
    private String longitude;
    private String latitude;
    private Integer distanceKm; // Changed to Integer to allow null
    // Pagination
    private int page = 0;
    private int size = 10;
    private String sortBy = "date";
    private String direction = "desc"; // asc or desc
}
