package com.example.demo.controller;

import com.example.demo.dto.event.*;
import com.example.demo.payload.ApiResponse;
import com.example.demo.payload.PagedResponse;
import com.example.demo.service.EventService;
import com.example.demo.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SeatService seatService;

    /**
     * ✅ Create new event
     * POST /api/events
     */
    @PostMapping("/create")
    public ApiResponse<EventResponse> createEvent(@RequestBody CreateEventDTO createEventDTO) {
        return eventService.createEvent(createEventDTO);
    }

    /**
     * ✅ Update event
     * PUT /api/events/{eventId} (full update) or PATCH (partial update)
     */
    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventUpdateDTO eventUpdateDTO) {
        return eventService.updateEvent(eventId, eventUpdateDTO);
    }

    /**
     * ✅ Delete event
     * DELETE /api/events/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ApiResponse.success("Event deleted successfully");
    }

    /**
     * ✅ Get event by ID
     * GET /api/events/{eventId}
     */
    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId)
                .map(eventResponse -> ApiResponse.success("Event retrieved successfully", eventResponse))
                .orElse(ApiResponse.error("Event not found"));
    }

    /**
     * ✅ Get all events with pagination and sorting
     * GET /api/events?page=0&size=10&sortBy=createdAt&direction=desc
     * Supports geolocation filtering: longitude, latitude, distanceKm
     */
    @GetMapping("/")
    public ResponseEntity<PagedResponse<EventResponse>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String longitude,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) Integer distanceKm
    ) {
        // If geolocation params provided, use geolocation filtering
        if (longitude != null && latitude != null && distanceKm != null) {
            EventSearchCriteria criteria = EventSearchCriteria.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .direction(direction)
                    .longitude(longitude)
                    .latitude(latitude)
                    .distanceKm(distanceKm)
                    .build();
            
            Page<EventResponse> eventPage = eventService.searchEventsWithPagination(criteria);
            
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(eventPage.getContent())
                    .page(eventPage.getNumber())
                    .size(eventPage.getSize())
                    .totalElements(eventPage.getTotalElements())
                    .totalPages(eventPage.getTotalPages())
                    .first(eventPage.isFirst())
                    .last(eventPage.isLast())
                    .empty(eventPage.isEmpty())
                    .build();
                    
            return ResponseEntity.ok(response);
        }
        
        // Normal pagination without geolocation
        Page<EventResponse> eventPage = eventService.getEventsWithPagination(page, size, sortBy, direction);
        
        PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                .content(eventPage.getContent())
                .page(eventPage.getNumber())
                .size(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .first(eventPage.isFirst())
                .last(eventPage.isLast())
                .empty(eventPage.isEmpty())
                .build();
                
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Search events with filters (REST-ful với query params)
     * GET /api/events/search?keyword=music&location=hanoi&eventType=concert&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<EventResponse>> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String eventStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        EventSearchCriteria criteria = EventSearchCriteria.builder()
                .keyword(keyword)
                .location(location)
                .eventType(eventType)
                .eventStatus(eventStatus)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(direction)
                .build();
                
        Page<EventResponse> eventPage = eventService.searchEventsWithPagination(criteria);
        
        PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                .content(eventPage.getContent())
                .page(eventPage.getNumber())
                .size(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .first(eventPage.isFirst())
                .last(eventPage.isLast())
                .empty(eventPage.isEmpty())
                .build();
                
        return ResponseEntity.ok(response);
    }


    /**
     * ✅ Caching event for booking service
     * GET /api/events/cache/{eventId}
     */
    @GetMapping("/cache/{eventId}")
    public ApiResponse<String> warmUpSeatCacheForEvent(@PathVariable Long eventId) {
        return seatService.warmUpSeatStatusCacheForEvent(eventId);
    }


    @GetMapping("/dashboard")
    public ApiResponse<EventDashboardDto> getEventsDashboardData() {

        EventDashboardDto data = new EventDashboardDto(
                15,
                5,
                3
        );

        return ApiResponse.success(
                "Get full data event Dashboard",
                data
        );
//        return eventService.getEventsDashboardData();
    }

    @GetMapping("/dashboard/next")
    public ApiResponse<UpcomingEventDashboardDto> getNextEventsDashboardData() {

        UpcomingEventDashboardDto data = new UpcomingEventDashboardDto(
                20,
                8,
                12
        );
        return ApiResponse.success(
                "Get next events for Dashboard",
                data
        );
//        return eventService.getNextEventsDashboardData();
    }








    /**
     * ✅ Get upcoming events for homepage
     * GET /api/events/upcoming?limit=6
     */
//    @GetMapping("/upcoming")
//    public ApiResponse<List<EventResponse>> getUpcomingEvents(
//            @RequestParam(defaultValue = "6") int limit
//    ) {
//        return ApiResponse.success("Upcoming events retrieved", eventService.getUpcomingEvents(limit));
//    }

    /**
     * ✅ Get featured/popular events for homepage
     * GET /api/events/featured?limit=4
     */
//    @GetMapping("/featured")
//    public ApiResponse<List<EventResponse>> getFeaturedEvents(
//            @RequestParam(defaultValue = "4") int limit
//    ) {
//        return ApiResponse.success("Featured events retrieved", eventService.getFeaturedEvents(limit));
//    }


}
