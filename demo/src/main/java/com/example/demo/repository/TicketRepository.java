package com.example.demo.repository;

import com.example.demo.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    @Query("SELECT t FROM TicketEntity t WHERE t.order.orderId = :orderId AND t.order.user.userId = :userId")
    List<TicketEntity> getTicketsByOrderId(Long orderId, Long userId);
}
