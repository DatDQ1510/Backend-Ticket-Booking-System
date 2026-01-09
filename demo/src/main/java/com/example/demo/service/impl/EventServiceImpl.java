package com.example.demo.service.impl;

import com.example.demo.context.UserContext;
import com.example.demo.dto.event.*;
import com.example.demo.entity.EventEntity;
import com.example.demo.entity.PictureEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EventMapper;
import com.example.demo.payload.ApiResponse;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PictureRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EventService;
import com.example.demo.specification.EventSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final PictureRepository pictureRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<EventResponse> searchEvents(EventSearchCriteria criteria) {
        Specification<EventEntity> spec = Specification
                .where(EventSpecification.hasLocation(criteria.getLocation()))
                .and(EventSpecification.hasEventType(criteria.getEventType()))
                .and(EventSpecification.afterDate(criteria.getStartDate()))
                .and(EventSpecification.beforeDate(criteria.getEndDate()))
                .and(EventSpecification.keywordInTitleOrDescription(criteria.getKeyword()))
                .and(EventSpecification.distanceEvent(criteria.getLongitude(), criteria.getLatitude(), criteria.getDistanceKm()));

        List<EventEntity> results = eventRepository.findAll(spec);

        // chuyển sang DTO bằng mapper
        return eventMapper.toResponseList(results);
    }


    /**
     * Create new event
     * ⭐ Uses UserContext to get current user automatically!
     *
     * @param request Event data
     * @param authHeader Not needed anymore (kept for backward compatibility)
     * @return Created event response
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ApiResponse<EventResponse> createEvent(CreateEventDTO request) {

        Long userId = UserContext.requireCurrentUserId();
        log.info("Creating event for userId: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        EventEntity eventEntity = eventMapper.toEntity(request);

        eventEntity.setUser(user);
        
        // Calculate geohash if longitude and latitude are provided
        if (request.getLongitude() != null && request.getLatitude() != null) {
            String geohash = com.example.demo.util.GeoHash.encode(
                request.getLatitude(), 
                request.getLongitude(), 
                5 
            );
            eventEntity.setGeohash(geohash);
            log.info("Calculated geohash: {} for event", geohash);
        }

        EventEntity savedEvent = eventRepository.save(eventEntity);
        log.info("Event created successfully with id: {}", savedEvent.getEventId());

        if(request.getPictures() != null && !request.getPictures().isEmpty()) {
            List<PictureEntity> pictures = request.getPictures().stream()
                    .map(pictureDTO -> {
                        PictureEntity pictureEntity = new PictureEntity();
                        pictureEntity.setPictureUrl(pictureDTO.getPictureUrl());
                        pictureEntity.setPublicId(pictureDTO.getPublicId());
                        pictureEntity.setMain(pictureDTO.isMain());
                        pictureEntity.setEvent(savedEvent);
                        return pictureEntity;
                    })
                    .collect(toList());
            savedEvent.setPictureUrls(pictures);
            pictureRepository.saveAll(pictures);

            log.info("Associated {} pictures with event id: {}", pictures.size(), savedEvent.getEventId());
        }
        EventResponse response = eventMapper.toResponse(savedEvent);

        return ApiResponse.success("Event created successfully", response);
    }

    @Override
    public Optional<EventResponse> getEventById(Long eventId) {

        // check cache first
        String value = cacheEventResponse(eventId);
        if(value != null) {
            log.info("Cache hit for event id: {}", eventId);
            EventResponse eventResponse = null;
            try {
                eventResponse = objectMapper.readValue(value, EventResponse.class);
            } catch (Exception e) {
                log.error("Error deserializing cached event response for event id: {}", eventId, e);
            }
            return Optional.ofNullable(eventResponse);
        }

        // cache miss, fetch from DB
        log.info("Cache miss for event id: {}", eventId);
        EventEntity  event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        EventResponse response = eventMapper.toResponse(event);

        // store in cache
        String cacheKey = buildEventCacheKey(eventId);
        try {
            String valueToCache = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, valueToCache, Duration.ofHours(1));
            log.info("Stored event id: {} in cache", eventId);
        } catch (Exception e) {
            log.error("Error serializing event response for caching. eventId={}", eventId, e);
        }

        return Optional.of(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isEventOwner(#eventId)")
    @Override
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
        redisTemplate.delete(buildEventCacheKey(eventId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @securityService.isEventOwner(#eventId)")
    public ApiResponse<EventResponse> updateEvent(Long eventId, EventUpdateDTO eventUpdateDTO) {
        EventEntity existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        eventMapper.updateEventFromDto(eventUpdateDTO, existingEvent);
        
        // Recalculate geohash if longitude and latitude are provided
        if (eventUpdateDTO.getLongitude() != null && eventUpdateDTO.getLatitude() != null) {
            String geohash = com.example.demo.util.GeoHash.encode(
                eventUpdateDTO.getLatitude(), 
                eventUpdateDTO.getLongitude(), 
                7 // Use precision 7 for storage (±76m accuracy)
            );
            existingEvent.setGeohash(geohash);
            log.info("Updated geohash: {} for event id: {}", geohash, eventId);
        }
        
        eventRepository.save(existingEvent);
        redisTemplate.delete(buildEventCacheKey(eventId));

        EventResponse response = eventMapper.toResponse(existingEvent);
        return ApiResponse.success("Event updated successfully", response);
    }

    @Override
    public Page<EventEntity> getEvents(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return eventRepository.findAll(pageable);
    }

    @Override
    public Page<EventSummaryDTO> getSummaryEvents(int limit, int offset, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of((int) Math.floor((double) offset / limit), limit, sort);
        Page<EventEntity> pageEvent = eventRepository.findAllWithMainPicture(pageable);

        return pageEvent.map(event -> {
            String mainPictureUrl = null;
            if (event.getPictureUrls() != null && !event.getPictureUrls().isEmpty()) {
                mainPictureUrl = event.getPictureUrls().stream()
                        .filter(PictureEntity::isMain)
                        .map(PictureEntity::getPictureUrl)
                        .findFirst()
                        .orElse(null);
            }

            return EventSummaryDTO.builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .location(event.getLocation())
                    .tag(event.getEventType())
                    .mainPictureUrl(mainPictureUrl)
                    .subTitle(event.getSubTitle())
                    .build();
        });
    }

    @Override
    public Page<EventResponse> getEventsWithPagination(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EventEntity> eventPage = eventRepository.findAllWithMainPicture(pageable);
        return eventPage.map(eventMapper::toResponse);
    }

    @Override
    public Page<EventResponse> searchEventsWithPagination(EventSearchCriteria criteria) {
        System.out.println("=== Search Events With Pagination ===");
        System.out.println("Longitude: " + criteria.getLongitude());
        System.out.println("Latitude: " + criteria.getLatitude());
        System.out.println("Distance: " + criteria.getDistanceKm());
        
        Specification<EventEntity> spec = Specification
                .where(EventSpecification.hasLocation(criteria.getLocation()))
                .and(EventSpecification.hasEventType(criteria.getEventType()))
                .and(EventSpecification.afterDate(criteria.getStartDate()))
                .and(EventSpecification.beforeDate(criteria.getEndDate()))
                .and(EventSpecification.keywordInTitleOrDescription(criteria.getKeyword()))
                .and(EventSpecification.distanceEvent(criteria.getLongitude(), criteria.getLatitude(), criteria.getDistanceKm()));
        Sort sort = criteria.getDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        Page<EventEntity> eventPage = eventRepository.findAll(spec, pageable);
        
        System.out.println("Found events: " + eventPage.getTotalElements());
        
        return eventPage.map(eventMapper::toResponse);
    }

    @Override
    public ApiResponse<?> getEventsDashboardData() {
        LocalDate today = LocalDate.now();
        // Tháng này

        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);

        // Tháng trước (cùng kỳ)
        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).atTime(23, 59, 59);

        return ApiResponse.success("Get full data event Dashboard",
                eventRepository.getEventsStatsMTD(
                        startThisMonth,
                        endThisMonth,
                        startLastMonth,
                        endLastMonth));
    }

    @Override
    public ApiResponse<?> getNextEventsDashboardData() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime startThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endThisMonth = today.atTime(23, 59, 59);

        LocalDateTime startLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endLastMonth = today.minusMonths(1).atTime(23, 59, 59);
        return ApiResponse.success("Get next events data for Dashboard",
                eventRepository.getNextEventsStatsMTD(
                        startThisMonth,
                        endThisMonth,
                        startLastMonth,
                        endLastMonth,
                        today));
    }

    private String cacheEventResponse(Long eventId) {
        // Implement caching logic ( using Redis )
        String cacheKey = buildEventCacheKey(eventId);
        String result = redisTemplate.opsForValue().get(cacheKey);
        if (result == null) {
            log.info("No cached event found for key: {}", cacheKey);
            return null;
        }
        return result;
    }

    private String buildEventCacheKey(Long eventId) {
        return "event:" + eventId;
    }


}
