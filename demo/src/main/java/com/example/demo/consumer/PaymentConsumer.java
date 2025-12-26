package com.example.demo.consumer;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.PaymentNotificationDTO;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentNotification(PaymentNotificationDTO notification) {
        System.out.println(">>> [PAYMENT CONSUMER] Nhận được payment notification:");
        System.out.println("    OrderId: " + notification.getOrderId());
        System.out.println("    ResultCode: " + notification.getResultCode());
        System.out.println("    TransId: " + notification.getTransId());
        System.out.println("    Amount: " + notification.getAmount());

        try {
        // Thanh toán thất bại
        System.out.println("❌ Xử lý thanh toán thất bại cho orderId: " + notification.getOrderId());
        System.out.println("   ResultCode: " + notification.getResultCode());

        // Cập nhật trạng thái thất bại
        orderService.updateOrderPaymentSuccess(notification.getOrderId(), notification.getTransId());

        System.out.println("❌ Đã cập nhật trạng thái thất bại cho order: " + notification.getOrderId());

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý payment notification: " + e.getMessage());
            e.printStackTrace();
            // Nếu có lỗi, message sẽ được retry hoặc gửi vào DLQ theo config
            throw new RuntimeException("Failed to process payment notification", e);
        }
    }
}
