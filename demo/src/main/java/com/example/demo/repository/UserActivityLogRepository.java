package com.example.demo.repository;

import com.example.demo.entity.UserActivityLog;
import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    /**
     * Tìm activity log mới nhất của user
     */
    Optional<UserActivityLog> findTopByUserOrderByActivityTimestampDesc(UserEntity user);

    /**
     * Tìm activity log mới nhất của user trong khoảng thời gian
     */
    @Query("SELECT u FROM UserActivityLog u WHERE u.user = :user AND u.activityTimestamp >= :since ORDER BY u.activityTimestamp DESC")
    Optional<UserActivityLog> findLatestActivitySince(@Param("user") UserEntity user, @Param("since") LocalDateTime since);

    /**
     * Count distinct users who had activity in date range
     */
    @Query("SELECT COUNT(DISTINCT u.user.userId) FROM UserActivityLog u WHERE u.activityTimestamp BETWEEN :start AND :end")
    Integer countDistinctUsersByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
