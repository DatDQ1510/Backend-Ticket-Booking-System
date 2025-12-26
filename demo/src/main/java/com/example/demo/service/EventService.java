package com.example.demo.service;

import com.example.demo.dto.event.*;
import com.example.demo.entity.EventEntity;
import com.example.demo.payload.ApiResponse;
import com.sun.jdi.request.EventRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventService {
    List<EventResponse> searchEvents(EventSearchCriteria criteria);

    ApiResponse<EventResponse> createEvent(CreateEventDTO createEvent);

    Optional<EventResponse> getEventById(Long eventId);

    void deleteEvent(Long eventId);

    ApiResponse<EventResponse> updateEvent(Long eventId, EventUpdateDTO eventUpdateDTO);

    Page<EventEntity> getEvents(int page, int size, String sortBy, String direction);

    Page<EventSummaryDTO> getSummaryEvents(int limit, int offset, String sortBy, String direction);

    Page<EventResponse> getEventsWithPagination(int page, int size, String sortBy, String direction);

    Page<EventResponse> searchEventsWithPagination(EventSearchCriteria criteria);
}
