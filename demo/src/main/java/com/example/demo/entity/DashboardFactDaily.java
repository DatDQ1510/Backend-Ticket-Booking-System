package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_fact_daily")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DashboardFactDaily {

    @Id
    @Column(name = "date_data", nullable = false)
    private LocalDate dateData;

    @Builder.Default
    @Column(name = "new_users", nullable = false)
    private Integer newUsers = 0;

    @Builder.Default
    @Column(name = "active_users", nullable = false)
    private Integer activeUsers = 0;

    @Builder.Default
    @Column(name = "new_events", nullable = false)
    private Integer newEvents = 0;

    @Builder.Default
    @Column(name = "active_events", nullable = false)
    private Integer activeEvents = 0;


    @Builder.Default
    @Column(name = "new_orders", nullable = false)
    private Integer newOrders = 0;

    @Builder.Default
    @Column(name = "completed_orders", nullable = false)
    private Integer completedOrders = 0;

    @Builder.Default
    @Column(name = "cancelled_orders", nullable = false)
    private Integer cancelledOrders = 0;

    @Builder.Default
    @Column(name = "revenue_day", precision = 15, scale = 2, nullable = false)
    private BigDecimal revenueDay = BigDecimal.valueOf(0);

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

