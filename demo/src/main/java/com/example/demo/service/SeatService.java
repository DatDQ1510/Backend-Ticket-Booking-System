package com.example.demo.service;

import com.example.demo.dto.seat.CreateSeatDTO;
import com.example.demo.entity.SeatEntity;
import com.example.demo.payload.ApiResponse;

import java.util.List;

public interface SeatService {
    List<SeatEntity> createSeats(CreateSeatDTO seatDTO);

    List<SeatEntity> updateSeats(Long[] seatIds, String status);

    List<SeatEntity> reserveSeats(List<Long> seatIds);
    
    List<SeatEntity> getSeatsByEventId(Long eventId);
    
    List<SeatEntity> getAvailableSeatsByEventId(Long eventId);

    /**
     * Warm up Redis cache for seat status by event.
     * This is primarily for the booking flow to avoid hitting DB repeatedly.
     */
    ApiResponse<String> warmUpSeatStatusCacheForEvent(Long eventId);
}
