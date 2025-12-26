package com.example.demo.repository;

import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByEvent_EventId(Long eventId);
    List<SeatEntity> findByEvent_EventIdAndStatus(Long eventId, SeatStatus status);
}
