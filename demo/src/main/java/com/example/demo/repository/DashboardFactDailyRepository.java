package com.example.demo.repository;

import com.example.demo.entity.DashboardFactDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardFactDailyRepository extends JpaRepository<DashboardFactDaily, LocalDate> {

    /**
     * Tìm dashboard fact theo ngày
     */
    Optional<DashboardFactDaily> findByDateData(LocalDate date);

    /**
     * Lấy dashboard facts trong khoảng thời gian
     */
    List<DashboardFactDaily> findByDateDataBetweenOrderByDateDataAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy N ngày gần nhất
     */
    @Query("SELECT d FROM DashboardFactDaily d ORDER BY d.dateData DESC")
    List<DashboardFactDaily> findLatestDays(@Param("limit") int limit);

    /**
     * Tính tổng revenue trong khoảng thời gian
     */
    @Query("SELECT SUM(d.revenueDay) FROM DashboardFactDaily d WHERE d.dateData BETWEEN :startDate AND :endDate")
    Long sumRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng orders trong khoảng thời gian
     */
    @Query("SELECT SUM(d.completedOrders) FROM DashboardFactDaily d WHERE d.dateData BETWEEN :startDate AND :endDate")
    Long sumCompletedOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng new users trong khoảng thời gian
     */
    @Query("SELECT SUM(d.newUsers) FROM DashboardFactDaily d WHERE d.dateData BETWEEN :startDate AND :endDate")
    Long sumNewUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tính tổng new events trong khoảng thời gian
     */
    @Query("SELECT SUM(d.newEvents) FROM DashboardFactDaily d WHERE d.dateData BETWEEN :startDate AND :endDate")
    Long sumNewEventsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Xóa dữ liệu cũ (cleanup)
     */
    @Query("DELETE FROM DashboardFactDaily d WHERE d.dateData < :beforeDate")
    void deleteOlderThan(@Param("beforeDate") LocalDate beforeDate);
}
