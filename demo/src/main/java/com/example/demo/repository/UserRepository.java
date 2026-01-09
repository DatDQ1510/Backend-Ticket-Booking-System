package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByHasPassword(boolean hasPassword);

    @Query(value = """
        SELECT
          (SELECT COUNT(*)
           FROM users
          ) AS total_users,
                    
          (SELECT COUNT(*)
           FROM users
           WHERE created_at BETWEEN :startThisMonth AND :endThisMonth
          ) AS users_this_month,

          (SELECT COUNT(*)
           FROM users
           WHERE created_at BETWEEN :startLastMonth AND :endLastMonth
          ) AS users_last_month
          

        """, nativeQuery = true)
    Map<String, Object> getUserStatsMTD(
            @Param("startThisMonth") LocalDateTime startThisMonth,
            @Param("endThisMonth") LocalDateTime endThisMonth,
            @Param("startLastMonth") LocalDateTime startLastMonth,
            @Param("endLastMonth") LocalDateTime endLastMonth
    );
}
