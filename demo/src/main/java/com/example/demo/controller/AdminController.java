package com.example.demo.controller;

import com.example.demo.dto.dashboard.*;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin Controller - Handles admin dashboard and analytics endpoints
 * All endpoints require ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;

    /**
     * Get comprehensive dashboard statistics
     * GET /api/admin/statistics/dashboard
     */
    @GetMapping("/statistics/dashboard")
    public ApiResponse<AdminDashboardStatsDTO> getDashboardStats() {
        log.info("📊 Admin requesting dashboard statistics");
        AdminDashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ApiResponse.success("Dashboard statistics retrieved successfully", stats);
    }

    /**
     * Get revenue chart data
     * GET /api/admin/statistics/revenue?period=day&startDate=2024-01-01&endDate=2024-01-31
     * 
     * @param period - "day", "week", or "month" (default: "day")
     * @param startDate - Start date (default: 30 days ago)
     * @param endDate - End date (default: today)
     */
    @GetMapping("/statistics/revenue")
    public ApiResponse<RevenueChartDTO> getRevenueChart(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📈 Admin requesting revenue chart: period={}, startDate={}, endDate={}", 
                period, startDate, endDate);

        // Default date range: last 30 days
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        RevenueChartDTO chartData = dashboardService.getRevenueChart(period, startDate, endDate);
        return ApiResponse.success("Revenue chart data retrieved successfully", chartData);
    }

    /**
     * Get top events by revenue
     * GET /api/admin/statistics/top-events?limit=5
     * 
     * @param limit - Number of top events to return (default: 5)
     */
    @GetMapping("/statistics/top-events")
    public ApiResponse<List<TopEventDTO>> getTopEvents(
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("🏆 Admin requesting top {} events", limit);
        List<TopEventDTO> topEvents = dashboardService.getTopEvents(limit);
        return ApiResponse.success("Top events retrieved successfully", topEvents);
    }

    /**
     * Get recent activities
     * GET /api/admin/statistics/activities?limit=10
     * 
     * @param limit - Number of activities to return (default: 10)
     */
    @GetMapping("/statistics/activities")
    public ApiResponse<List<RecentActivityDTO>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("🔔 Admin requesting recent activities (limit: {})", limit);
        List<RecentActivityDTO> activities = dashboardService.getRecentActivities(limit);
        return ApiResponse.success("Recent activities retrieved successfully", activities);
    }

    /**
     * Health check for admin endpoints
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("Admin API is healthy", "OK");
    }
}
