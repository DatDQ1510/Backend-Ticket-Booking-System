package com.example.demo.repository;

import com.example.demo.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    Optional<List<EventEntity>> findByEventType (String eventType);
    Optional<List<EventEntity>> findByLocation (String location);
    List<EventEntity> findByDate(Date date);
    List<EventEntity> findByDateBetween(Date start, Date end);

    @Query("""

            SELECT e FROM EventEntity e 
            LEFT JOIN FETCH e.pictureUrls p
            WHERE p.isMain = true or p IS NULL
    """)
    Page<EventEntity> findAllWithMainPicture(Pageable pageable);

}
