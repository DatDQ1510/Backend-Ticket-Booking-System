package com.example.demo.service.impl;

import com.example.demo.dto.seat.CreateSeatDTO;
import com.example.demo.entity.EventEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.enums.SeatStatus;
import com.example.demo.payload.ApiResponse;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.service.SeatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<SeatEntity> createSeats(CreateSeatDTO seatDTO) {
        Long eventId = seatDTO.getEventId();
        int numRows = seatDTO.getNumRows();
        int seatsPerRow = seatDTO.getSeatsPerRow();
        Long basePrice = seatDTO.getBasePrice();
        String seatType = seatDTO.getSeatType();
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<SeatEntity> seats = new ArrayList<>();

        for (char row = 'A'; row < 'A' + numRows; row++) {
            for (int num = 1; num <= seatsPerRow; num++) {
                String seatNumber = row + String.valueOf(num);

                SeatEntity seat = SeatEntity.builder()
                        .seatNumber(seatNumber)
                        .seatRow(String.valueOf(row))
                        .seatType(seatType)
                        .price(basePrice)
                        .status(SeatStatus.AVAILABLE)
                        .event(event)
                        .build();

                seats.add(seat);
            }
        }

        return seatRepository.saveAll(seats);
    }

    @Override
    public List<SeatEntity> updateSeats(Long[] seatIds, String status) {
        List<SeatEntity> updatedSeats = new ArrayList<>();

        for(Long seatId : seatIds){
            SeatEntity seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found"));
            seat.setStatus(SeatStatus.valueOf(status.toUpperCase()));
            updatedSeats.add(seat);
        }

        return seatRepository.saveAll(updatedSeats);
    }

    @Transactional
    public List<SeatEntity> reserveSeats(List<Long> seatIds) {
        List<SeatEntity> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (SeatEntity seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
            seat.setStatus(SeatStatus.BOOKED);
        }

        return seatRepository.saveAll(seats);
    }

    @Override
    public List<SeatEntity> getSeatsByEventId(Long eventId) {
        return seatRepository.findByEvent_EventId(eventId);
    }

    @Override
    public List<SeatEntity> getAvailableSeatsByEventId(Long eventId) {
        return seatRepository.findByEvent_EventIdAndStatus(eventId, SeatStatus.AVAILABLE);
    }

    @Override
    public ApiResponse<String> warmUpSeatStatusCacheForEvent(Long eventId) {
        List<SeatEntity> seats = seatRepository.findByEvent_EventId(eventId);
        if (seats == null || seats.isEmpty()) {
            return ApiResponse.error("No seats found for eventId: " + eventId);
        }

        for (SeatEntity seat : seats) {
            redisTemplate.opsForValue().set(
                    "event:" + eventId + "::seat:" + seat.getSeatId(),
                    seat.getStatus().toString(),
                    1,
                    TimeUnit.HOURS
            );
        }

        return ApiResponse.success("Seat status cache warmed up", "eventId=" + eventId + ", seats=" + seats.size());
    }

}
