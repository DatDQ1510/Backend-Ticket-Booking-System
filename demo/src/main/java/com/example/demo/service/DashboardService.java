package com.example.demo.service;

import com.example.demo.dto.dashboard.*;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service - Provides analytics and statistics for admin dashboard
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final DashboardFactDailyRepository factDailyRepository;
    private final UserActivityLogRepository activityLogRepository;

    /**
     * Get comprehensive dashboard statistics
     */
    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getDashboardStats() {
        log.info("📊 Fetching admin dashboard statistics");

        // Get date ranges
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfThisMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = now.toLocalDate().atTime(23, 59, 59);

        // Fetch user stats
        Map<String, Object> userStats = userRepository.getUserStatsMTD(
                startOfThisMonth, endOfThisMonth,
                startOfLastMonth, endOfLastMonth
        );

        // Fetch event stats
        Map<String, Object> eventStats = eventRepository.getEventStatsMTD(
                startOfThisMonth, endOfThisMonth,
                startOfLastMonth, endOfLastMonth
        );

        // Fetch order/revenue stats
        Map<String, Object> orderStats = orderRepository.getOrderStatsMTD(
                startOfThisMonth, endOfThisMonth,
                startOfLastMonth, endOfLastMonth
        );

        // Get today's stats
        Map<String, Object> todayStats = orderRepository.getOrderStatsMTD(
                startOfToday, endOfToday,
                startOfToday.minusDays(1), startOfToday.minusNanos(1)
        );

        // Get total bookings and tickets
        Long totalBookings = orderRepository.count();
        Long totalTickets = ticketRepository.count();

        // Get active events count (events in the future)
        Long activeEvents = eventRepository.countByDateAfter(LocalDate.now());

        // Build response
        Long totalUsers = getLongValue(userStats, "total_users");
        Long totalEvents = getLongValue(eventStats, "total_events");
        BigDecimal totalRevenue = getBigDecimalValue(orderStats, "total_revenue");
        
        Long monthNewUsers = getLongValue(userStats, "users_this_month");
        Long lastMonthNewUsers = getLongValue(userStats, "users_last_month");
        
        Long monthNewEvents = getLongValue(eventStats, "events_this_month");
        Long lastMonthNewEvents = getLongValue(eventStats, "events_last_month");
        
        BigDecimal monthRevenue = getBigDecimalValue(orderStats, "revenue_this_month");
        BigDecimal lastMonthRevenue = getBigDecimalValue(orderStats, "revenue_last_month");
        
        BigDecimal todayRevenue = getBigDecimalValue(todayStats, "revenue_this_month");

        // Calculate growth rates
        Double revenueGrowth = calculateGrowth(monthRevenue, lastMonthRevenue);
        Double userGrowth = calculateGrowth(monthNewUsers, lastMonthNewUsers);
        Double eventGrowth = calculateGrowth(monthNewEvents, lastMonthNewEvents);

        log.info("✅ Dashboard stats fetched successfully");

        return AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers != null ? totalUsers : 0L)
                .totalEvents(totalEvents != null ? totalEvents : 0L)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .activeEvents(activeEvents != null ? activeEvents : 0L)
                .totalBookings(totalBookings)
                .totalTicketsSold(totalTickets)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .todayBookings(0L)  // TODO: Implement if needed
                .monthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO)
                .monthBookings(0L)  // TODO: Implement if needed
                .monthNewUsers(monthNewUsers != null ? monthNewUsers : 0L)
                .monthNewEvents(monthNewEvents != null ? monthNewEvents : 0L)
                .lastMonthRevenue(lastMonthRevenue != null ? lastMonthRevenue : BigDecimal.ZERO)
                .lastMonthBookings(0L)  // TODO: Implement if needed
                .lastMonthNewUsers(lastMonthNewUsers != null ? lastMonthNewUsers : 0L)
                .lastMonthNewEvents(lastMonthNewEvents != null ? lastMonthNewEvents : 0L)
                .revenueGrowth(revenueGrowth)
                .userGrowth(userGrowth)
                .bookingGrowth(0.0)  // TODO: Implement if needed
                .eventGrowth(eventGrowth)
                .build();
    }

    /**
     * Get revenue chart data for specific period
     */
    @Transactional(readOnly = true)
    public RevenueChartDTO getRevenueChart(String period, LocalDate startDate, LocalDate endDate) {
        log.info("📈 Fetching revenue chart data for period: {}", period);

        List<DashboardFactDaily> facts = factDailyRepository.findByDateDataBetweenOrderByDateDataAsc(
                startDate, endDate
        );

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        if ("day".equals(period)) {
            // Daily data
            for (DashboardFactDaily fact : facts) {
                labels.add(fact.getDateData().format(DateTimeFormatter.ofPattern("dd/MM")));
                data.add(fact.getRevenueDay());
            }
        } else if ("week".equals(period)) {
            // Group by week
            Map<Integer, BigDecimal> weeklyData = new TreeMap<>();
            for (DashboardFactDaily fact : facts) {
                int weekOfYear = fact.getDateData().getDayOfYear() / 7;
                weeklyData.merge(weekOfYear, fact.getRevenueDay(), BigDecimal::add);
            }
            weeklyData.forEach((week, revenue) -> {
                labels.add("Tuần " + week);
                data.add(revenue);
            });
        } else if ("month".equals(period)) {
            // Group by month
            Map<YearMonth, BigDecimal> monthlyData = new TreeMap<>();
            for (DashboardFactDaily fact : facts) {
                YearMonth yearMonth = YearMonth.from(fact.getDateData());
                monthlyData.merge(yearMonth, fact.getRevenueDay(), BigDecimal::add);
            }
            monthlyData.forEach((month, revenue) -> {
                labels.add(month.format(DateTimeFormatter.ofPattern("MM/yyyy")));
                data.add(revenue);
            });
        }

        return RevenueChartDTO.builder()
                .labels(labels)
                .data(data)
                .period(period)
                .build();
    }

    /**
     * Get top events by revenue
     */
    @Transactional(readOnly = true)
    public List<TopEventDTO> getTopEvents(int limit) {
        log.info("🏆 Fetching top {} events", limit);

        List<Object[]> topEvents = orderRepository.getTopEventsByRevenue(limit);
        
        return topEvents.stream()
                .map(row -> {
                    Long eventId = ((Number) row[0]).longValue();
                    String title = (String) row[1];
                    BigDecimal totalRevenue = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
                    Long totalBookings = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                    Long ticketsSold = row[4] != null ? ((Number) row[4]).longValue() : 0L;

                    return TopEventDTO.builder()
                            .eventId(eventId)
                            .title(title)
                            .totalRevenue(totalRevenue)
                            .totalBookings(totalBookings)
                            .ticketsSold(ticketsSold)
                            .availableSeats(0)  // TODO: Calculate if needed
                            .occupancyRate(0.0)  // TODO: Calculate if needed
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get recent activities from various sources
     */
    @Transactional(readOnly = true)
    public List<RecentActivityDTO> getRecentActivities(int limit) {
        log.info("🔔 Fetching recent activities (limit: {})", limit);

        List<RecentActivityDTO> activities = new ArrayList<>();
        
        // Get recent user registrations
        List<UserEntity> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc();
        for (UserEntity user : recentUsers) {
            String displayName = user.getUsername() != null ? user.getUsername() : user.getEmail();
            activities.add(RecentActivityDTO.builder()
                    .id((long) activities.size() + 1)
                    .type("user_registration")
                    .message("Người dùng mới " + displayName + " đã đăng ký")
                    .timestamp(user.getCreatedAt())
                    .userId(user.getUserId())
                    .build());
        }

        // Get recent events
        List<EventEntity> recentEvents = eventRepository.findTop5ByOrderByCreatedAtDesc();
        for (EventEntity event : recentEvents) {
            activities.add(RecentActivityDTO.builder()
                    .id((long) activities.size() + 1)
                    .type("event_created")
                    .message("Sự kiện mới: " + event.getTitle())
                    .timestamp(event.getCreatedAt())
                    .eventId(event.getEventId())
                    .build());
        }

        // Get recent completed orders
        List<OrderEntity> recentOrders = orderRepository.findTop10ByStatusOrderByPaidAtDesc(OrderStatus.PAID);
        for (OrderEntity order : recentOrders) {
            // OrderEntity doesn't have createdAt, only paidAt is available
            if (order.getPaidAt() != null) {
                activities.add(RecentActivityDTO.builder()
                        .id((long) activities.size() + 1)
                        .type("payment_completed")
                        .message("Thanh toán hoàn thành: " + order.getAmount().longValue() + " VNĐ")
                        .timestamp(order.getPaidAt())
                        .bookingId(order.getOrderId())
                        .userId(order.getUser().getUserId())
                        .build());
            }
        }

        // Sort by timestamp descending and limit
        return activities.stream()
                .filter(a -> a.getTimestamp() != null)
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private Double calculateGrowth(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current != null && current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        if (current == null) return -100.0;
        
        double curr = current.doubleValue();
        double prev = previous.doubleValue();
        double growth = ((curr - prev) / prev) * 100;
        
        return Math.round(growth * 10.0) / 10.0;  // Round to 1 decimal place
    }

    private Double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (current == null) return -100.0;
        
        BigDecimal growth = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return growth.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
