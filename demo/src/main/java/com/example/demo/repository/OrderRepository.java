package com.example.demo.repository;


import com.example.demo.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Query(value = """
        SELECT
          (SELECT SUM(amount)
           FROM orders
          ) AS total_revenue,
                    
          (SELECT SUM(amount)
           FROM orders
           WHERE paid_at BETWEEN :startThisMonth AND :endThisMonth
          ) AS revenue_this_month,

          (SELECT SUM(amount)
           FROM orders
           WHERE paid_at BETWEEN :startLastMonth AND :endLastMonth
          ) AS revenue_last_month
          

        """, nativeQuery = true)
    Map<String, Object> getRevenueStatsMTD(
            @Param("startThisMonth") LocalDateTime startThisMonth,
            @Param("endThisMonth") LocalDateTime endThisMonth,
            @Param("startLastMonth") LocalDateTime startLastMonth,
            @Param("endLastMonth") LocalDateTime endLastMonth
    );
}
