package com.example.demo.repository;


import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserUserIdOrderByOrderIdDesc(Long userId);
    
    // For seat hold expiration task
    List<OrderEntity> findByStatusIn(List<OrderStatus> statuses);
    
    List<OrderEntity> findByStatusAndPaidAtBefore(OrderStatus status, LocalDateTime createdAt);
    
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
    Map<String, Object> getOrderStatsMTD(
            @Param("startThisMonth") LocalDateTime startThisMonth,
            @Param("endThisMonth") LocalDateTime endThisMonth,
            @Param("startLastMonth") LocalDateTime startLastMonth,
            @Param("endLastMonth") LocalDateTime endLastMonth
    );

    /**
     * Get top events by revenue
     */
    @Query(value = """
        SELECT 
            e.event_id,
            e.event_name,
            COALESCE(SUM(o.amount), 0) as total_revenue,
            COUNT(DISTINCT o.order_id) as total_bookings,
            COUNT(t.ticket_id) as tickets_sold
        FROM events e
        LEFT JOIN tickets t ON e.event_id = t.event_id
        LEFT JOIN orders o ON t.order_id = o.order_id AND o.status = 'COMPLETED'
        GROUP BY e.event_id, e.event_name
        ORDER BY total_revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopEventsByRevenue(@Param("limit") int limit);

    /**
     * Find top N recent completed orders
     */
    List<OrderEntity> findTop10ByStatusOrderByPaidAtDesc(OrderStatus status);
}
