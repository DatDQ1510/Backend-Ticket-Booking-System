package com.example.demo.consumer;


import com.example.demo.config.RabbitMQConfig;
import com.example.demo.custom.MailService;
import com.example.demo.dto.PaymentNotificationDTO;
import com.example.demo.entity.OrderEntity;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final MailService mailService;
    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    @Transactional(readOnly = true) // Cần transaction để tránh LazyInitializationException
    public void handleEmailNotification(PaymentNotificationDTO notification) {
        System.out.println(">>> [EMAIL CONSUMER] Nhận được payment notification để gửi email:");
        System.out.println("    OrderId: " + notification.getOrderId());
        System.out.println("    ResultCode: " + notification.getResultCode());
        System.out.println("    Amount: " + notification.getAmount());

        try {
            Long orderId = notification.getOrderId();
            Double amount = Double.parseDouble(notification.getAmount().toString());

            // ✅ Lấy order với FULL details (user + tickets + event + seat) để tránh LazyInitializationException
            OrderEntity order = orderRepository.findByIdWithFullDetails(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
            String email = order.getUser().getEmail();
            System.out.println("📧 Sending email to: " + email);
            
            // Kiểm tra resultCode để gửi email tương ứng
            if (notification.getResultCode() == 0) {
                // Thanh toán thành công - gửi email xác nhận
                System.out.println("📧 Gửi email xác nhận thanh toán thành công cho order: " + orderId);
                mailService.sendPaymentSuccessEmail(email, orderId, amount);
                System.out.println("✅ Đã gửi email xác nhận thanh toán thành công cho order: " + orderId);
            } else {
                // Thanh toán thất bại - gửi email thông báo thất bại
                System.out.println("📧 Gửi email thông báo thanh toán thất bại cho order: " + orderId);
                // TODO: Implement sendPaymentFailedEmail if needed
                // mailService.sendPaymentFailedEmail(email, orderId, amount);
                System.out.println("⚠️ Email thanh toán thất bại chưa được implement");
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            // ⚠️ Không throw exception để tránh retry vô hạn khi Gmail authentication failed
            // Nếu muốn retry: throw new RuntimeException("Failed to send email notification", e);
        }
    }
}
