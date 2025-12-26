package com.example.demo.repository;


import com.example.demo.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserUserIdOrderByOrderIdDesc(Long userId);
    
    @Query("SELECT o FROM OrderEntity o JOIN FETCH o.user WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdWithUser(@Param("orderId") Long orderId);
    
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "LEFT JOIN FETCH o.tickets t " +
           "LEFT JOIN FETCH t.event " +
           "LEFT JOIN FETCH t.seat " +
           "WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdWithTicketsAndDetails(@Param("orderId") Long orderId);
}
