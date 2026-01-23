package com.example.demo.service;

import com.example.demo.entity.UserActivityLog;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserActivityLogRepository;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // Chỉ log lại nếu activity cách nhau >= 5 phút
    private static final long MIN_LOG_INTERVAL_MINUTES = 5;

    /**
     * Ghi log hoạt động của user từ request
     * - Async để không block request
     * - Chỉ log nếu activity mới cách xa activity cũ >= 5 phút
     * - Sử dụng REQUIRES_NEW để tách transaction và noRollbackFor để tránh rollback
     */
    @Async
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        noRollbackFor = {Exception.class, org.springframework.dao.DataIntegrityViolationException.class}
    )
    public void logUserActivity(String email, HttpServletRequest request) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found for logging activity: {}", email);
                return;
            }

            UserEntity user = userOpt.get();
            
            // Kiểm tra xem có cần log không
            if (!shouldLogActivity(user)) {
                log.debug("Skipping activity log for {} - too soon since last activity", email);
                return;
            }

            // Tạo activity log
            String activityType = buildActivityType(request);
            UserActivityLog activityLog = UserActivityLog.builder()
                    .user(user)
                    .activityType(activityType)
                    .activityTimestamp(LocalDateTime.now())
                    .build();

            activityLogRepository.save(activityLog);
            log.info("✅ Logged activity for user: {} - {}", email, activityType);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle unique constraint violation gracefully (legacy constraint issue)
            log.debug("Activity log already exists for user {} (constraint violation) - skipping", email);
            // Không ném lại exception vì đã config noRollbackFor
        } catch (Exception e) {
            // Không throw exception để tránh ảnh hưởng request chính
            log.error("Failed to log user activity for {}: {}", email, e.getMessage());
            // Không ném lại exception vì đây là async và không nên fail
        }
    }
    private boolean shouldLogActivity(UserEntity user) {
        Optional<UserActivityLog> lastActivity = 
            activityLogRepository.findTopByUserOrderByActivityTimestampDesc(user);

        if (lastActivity.isEmpty()) {
            return true; // Chưa có activity → log
        }

        LocalDateTime lastTime = lastActivity.get().getActivityTimestamp();
        long minutesSinceLastActivity = ChronoUnit.MINUTES.between(lastTime, LocalDateTime.now());
        
        return minutesSinceLastActivity >= MIN_LOG_INTERVAL_MINUTES;
    }

    /**
     * Tạo mô tả activity type từ request
     */
    private String buildActivityType(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // Đơn giản hóa URI để dễ đọc
        if (uri.contains("/api/events")) {
            return "VIEW_EVENTS";
        } else if (uri.contains("/api/bookings")) {
            return method.equals("POST") ? "CREATE_BOOKING" : "VIEW_BOOKINGS";
        } else if (uri.contains("/api/tickets")) {
            return "VIEW_TICKETS";
        } else if (uri.contains("/api/users")) {
            return "VIEW_PROFILE";
        } else if (uri.contains("/api/payment")) {
            return "PAYMENT";
        } else {
            return "API_REQUEST";
        }
    }

    /**
     * Lấy activity log cuối cùng của user
     */
    @Transactional(readOnly = true)
    public Optional<UserActivityLog> getLastActivity(Long userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return activityLogRepository.findTopByUserOrderByActivityTimestampDesc(user.get());
    }

    /**
     * Lấy activity log cuối cùng của user theo email
     */
    @Transactional(readOnly = true)
    public Optional<UserActivityLog> getLastActivityByEmail(String email) {
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return activityLogRepository.findTopByUserOrderByActivityTimestampDesc(user.get());
    }
}
