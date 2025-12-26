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

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailNotification(PaymentNotificationDTO notification) {
        System.out.println(">>> [EMAIL CONSUMER] Nhận được payment notification để gửi email:");
        System.out.println("    OrderId: " + notification.getOrderId());
        System.out.println("    ResultCode: " + notification.getResultCode());
        System.out.println("    Amount: " + notification.getAmount());

        try {
            // Gửi email thành công
            Long orderId = Long.parseLong(notification.getOrderId().toString());
            Double amount = Double.parseDouble(notification.getAmount().toString());
            System.out.println("📧 Gửi email xác nhận thanh toán thành công cho order: " + orderId);

            // ✅ Lấy email từ Order entity với fetch join (không dùng UserContext vì consumer không có SecurityContext)
            OrderEntity order = orderRepository.findByIdWithUser(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
            String email = order.getUser().getEmail();
            System.out.println("📧 Sending email to: " + email);
            
            mailService.sendPaymentSuccessEmail(email, orderId, amount);

            System.out.println("✅ Đã gửi email thành công cho order: " + notification.getOrderId());


        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            // Nếu có lỗi, message sẽ được retry hoặc gửi vào DLQ theo config
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}
