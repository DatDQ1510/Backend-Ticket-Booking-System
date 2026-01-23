package com.example.demo.scheduler;

import com.example.demo.entity.DashboardFactDaily;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Fact Daily Calculator
 * Runs daily at midnight to calculate and store daily metrics
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardFactDailyScheduler {

    private final DashboardFactDailyRepository factDailyRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final UserActivityLogRepository activityLogRepository;

    /**
     * Calculate yesterday's dashboard facts
     * Runs every day at 00:05 AM
     */
    @Scheduled(cron = "0 5 0 * * *")  // At 00:05 every day
    @Transactional
    public void calculateYesterdayFacts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("🔄 Starting dashboard fact calculation for date: {}", yesterday);

        try {
            calculateAndSaveDailyFacts(yesterday);
            log.info("✅ Successfully calculated dashboard facts for {}", yesterday);
        } catch (Exception e) {
            log.error("❌ Failed to calculate dashboard facts for {}: {}", yesterday, e.getMessage(), e);
        }
    }

    /**
     * Backfill missing dashboard facts
     * Runs every day at 01:00 AM
     * Checks for any missing dates in the last 30 days and fills them
     */
    @Scheduled(cron = "0 0 1 * * *")  // At 01:00 every day
    @Transactional
    public void backfillMissingFacts() {
        log.info("🔄 Starting backfill for missing dashboard facts");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);

        int filled = 0;
        for (LocalDate date = startDate; date.isBefore(today); date = date.plusDays(1)) {
            if (factDailyRepository.findByDateData(date).isEmpty()) {
                try {
                    calculateAndSaveDailyFacts(date);
                    filled++;
                    log.info("✅ Backfilled dashboard facts for {}", date);
                } catch (Exception e) {
                    log.error("❌ Failed to backfill facts for {}: {}", date, e.getMessage());
                }
            }
        }

        log.info("✅ Backfill completed. Filled {} missing dates", filled);
    }

    /**
     * Calculate and save dashboard facts for a specific date
     */
    @Transactional
    public void calculateAndSaveDailyFacts(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Calculate new users
        Integer newUsers = countUsersCreatedBetween(startOfDay, endOfDay);

        // Calculate active users (users who had activity that day)
        Integer activeUsers = countActiveUsersBetween(startOfDay, endOfDay);

        // Calculate new events
        Integer newEvents = countEventsCreatedBetween(startOfDay, endOfDay);

        // Calculate active events (events that were scheduled for that day or later)
        Integer activeEvents = countActiveEventsOn(date);

        // Calculate new orders
        Integer newOrders = countOrdersCreatedBetween(startOfDay, endOfDay);

        // Calculate completed orders
        Integer completedOrders = countCompletedOrdersBetween(startOfDay, endOfDay);

        // Calculate cancelled orders
        Integer cancelledOrders = countCancelledOrdersBetween(startOfDay, endOfDay);

        // Calculate revenue (from completed orders)
        BigDecimal revenueDay = calculateRevenueBetween(startOfDay, endOfDay);

        // Create or update dashboard fact
        DashboardFactDaily fact = factDailyRepository.findByDateData(date)
                .orElse(DashboardFactDaily.builder()
                        .dateData(date)
                        .build());

        fact.setNewUsers(newUsers);
        fact.setActiveUsers(activeUsers);
        fact.setNewEvents(newEvents);
        fact.setActiveEvents(activeEvents);
        fact.setNewOrders(newOrders);
        fact.setCompletedOrders(completedOrders);
        fact.setCancelledOrders(cancelledOrders);
        fact.setRevenueDay(revenueDay);

        factDailyRepository.save(fact);

        log.info("💾 Saved dashboard fact for {}: newUsers={}, activeUsers={}, revenue={}", 
                date, newUsers, activeUsers, revenueDay);
    }

    // ==================== Helper Methods ====================

    private Integer countUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return (int) userRepository.count(); // TODO: Add query for date range
    }

    private Integer countActiveUsersBetween(LocalDateTime start, LocalDateTime end) {
        // Count users who had activity logs in the date range
        return activityLogRepository.countDistinctUsersByTimestampBetween(start, end);
    }

    private Integer countEventsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return (int) eventRepository.count(); // TODO: Add query for date range
    }

    private Integer countActiveEventsOn(LocalDate date) {
        return eventRepository.countByDateAfter(date).intValue();
    }

    private Integer countOrdersCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return (int) orderRepository.count(); // TODO: Add query for date range
    }

    private Integer countCompletedOrdersBetween(LocalDateTime start, LocalDateTime end) {
        List<OrderStatus> statuses = List.of(OrderStatus.PAID);
        return orderRepository.findByStatusIn(statuses).size(); // TODO: Add query for date range
    }

    private Integer countCancelledOrdersBetween(LocalDateTime start, LocalDateTime end) {
        List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.PAYMENT_FAILED, OrderStatus.WAITING_PAYMENT);
        return orderRepository.findByStatusIn(statuses).size(); // TODO: Add query for date range
    }

    private BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        // TODO: Add proper query to calculate revenue between dates
        // For now, return 0
        return BigDecimal.ZERO;
    }

    /**
     * Cleanup old dashboard facts (older than 1 year)
     * Runs on the 1st day of every month at 02:00 AM
     */
    @Scheduled(cron = "0 0 2 1 * *")  // At 02:00 on the 1st of every month
    @Transactional
    public void cleanupOldFacts() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        log.info("🗑️ Cleaning up dashboard facts older than {}", oneYearAgo);

        try {
            factDailyRepository.deleteOlderThan(oneYearAgo);
            log.info("✅ Successfully cleaned up old dashboard facts");
        } catch (Exception e) {
            log.error("❌ Failed to cleanup old facts: {}", e.getMessage(), e);
        }
    }
}
