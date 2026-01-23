package com.example.demo.repository;

import com.example.demo.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    Optional<List<EventEntity>> findByEventType (String eventType);
    Optional<List<EventEntity>> findByLocation (String location);
    List<EventEntity> findByDate(LocalDate date);
    List<EventEntity> findByDateBetween(LocalDate start, LocalDate end);

    @Query("""

            SELECT e FROM EventEntity e 
            LEFT JOIN FETCH e.pictureUrls p
            WHERE p.isMain = true or p IS NULL
    """)
    Page<EventEntity> findAllWithMainPicture(Pageable pageable);


    @Query(value = """
        SELECT
          (SELECT COUNT(*)
           FROM events
          ) AS total_events,
                    
          (SELECT COUNT(*)
           FROM events
           WHERE created_at BETWEEN :startThisMonth AND :endThisMonth
          ) AS events_this_month,

          (SELECT COUNT(*)
           FROM events
           WHERE created_at BETWEEN :startLastMonth AND :endLastMonth
          ) AS events_last_month
          

        """, nativeQuery = true)
    Map<String, Object> getEventStatsMTD(
            @Param("startThisMonth") LocalDateTime startThisMonth,
            @Param("endThisMonth") LocalDateTime endThisMonth,
            @Param("startLastMonth") LocalDateTime startLastMonth,
            @Param("endLastMonth") LocalDateTime endLastMonth
    );

    /**
     * Count active events (events in the future)
     */
    Long countByDateAfter(LocalDate date);

    /**
     * Find top 5 recently created events
     */
    List<EventEntity> findTop5ByOrderByCreatedAtDesc();


    @Query(value = """
    SELECT
      -- Tổng event chưa diễn ra (snapshot)
      (SELECT COUNT(*)
       FROM events
       WHERE date >= :now
      ) AS total_upcoming_events,

      -- Event tạo trong tháng này & chưa diễn ra
      (SELECT COUNT(*)
       FROM events
       WHERE created_at BETWEEN :startThisMonth AND :endThisMonth
         AND date >= :now
      ) AS events_this_month,

      -- Event tạo cùng kỳ tháng trước & chưa diễn ra
      (SELECT COUNT(*)
       FROM events
       WHERE created_at BETWEEN :startLastMonth AND :endLastMonth
         AND date >= :now
      ) AS events_last_month
    """, nativeQuery = true)
    Map<String, Object> getNextEventsStatsMTD(
            @Param("startThisMonth") LocalDateTime startThisMonth,
            @Param("endThisMonth") LocalDateTime endThisMonth,
            @Param("startLastMonth") LocalDateTime startLastMonth,
            @Param("endLastMonth") LocalDateTime endLastMonth,
            @Param("now") LocalDate now
    );


}
