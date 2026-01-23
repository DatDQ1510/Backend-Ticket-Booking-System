package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Admin Dashboard Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    // Tổng quan
    private Long totalUsers;
    private Long totalEvents;
    private BigDecimal totalRevenue;
    private Long activeEvents;
    private Long totalBookings;
    private Long totalTicketsSold;
    
    // Hôm nay
    private BigDecimal todayRevenue;
    private Long todayBookings;
    
    // Tháng này
    private BigDecimal monthRevenue;
    private Long monthBookings;
    private Long monthNewUsers;
    private Long monthNewEvents;
    
    // Tháng trước
    private BigDecimal lastMonthRevenue;
    private Long lastMonthBookings;
    private Long lastMonthNewUsers;
    private Long lastMonthNewEvents;
    
    // Growth rates (%)
    private Double revenueGrowth;
    private Double userGrowth;
    private Double bookingGrowth;
    private Double eventGrowth;
}
