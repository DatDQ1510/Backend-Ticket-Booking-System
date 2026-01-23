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
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🐰 [PAYMENT CONSUMER] NHẬN ĐƯỢC MESSAGE TỪ RABBITMQ     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("📦 Notification Details:");
        System.out.println("    OrderId: " + notification.getOrderId());
        System.out.println("    ResultCode: " + notification.getResultCode());
        System.out.println("    TransId: " + notification.getTransId());
        System.out.println("    Amount: " + notification.getAmount());
        System.out.println("    PaymentType: " + notification.getPaymentType());

        try {
            // Kiểm tra resultCode để xác định thanh toán thành công hay thất bại
            // resultCode = 0: Thành công
            // resultCode != 0: Thất bại
            if (notification.getResultCode() == 0) {
                // Thanh toán thành công
                System.out.println("\n✅✅✅ THANH TOÁN THÀNH CÔNG ✅✅✅");
                System.out.println("📌 OrderId: " + notification.getOrderId());
                System.out.println("📌 TransId: " + notification.getTransId());
                System.out.println("💰 Amount: " + notification.getAmount());
                System.out.println("\n🔄 Đang gọi orderService.updateOrderPaymentSuccess()...");
                
                // Cập nhật trạng thái thành công
                orderService.updateOrderPaymentSuccess(notification.getOrderId(), notification.getTransId());
                
                System.out.println("\n🎉🎉🎉 ĐÃ HOÀN TẤT CẬP NHẬT ORDER " + notification.getOrderId() + " SANG PAID 🎉🎉🎉\n");
            } else {
                // Thanh toán thất bại
                System.out.println("❌ Xử lý thanh toán thất bại cho orderId: " + notification.getOrderId());
                System.out.println("   ResultCode: " + notification.getResultCode());
                
                // Cập nhật trạng thái thất bại và giải phóng ghế
                orderService.updateOrderPaymentFailed(notification.getOrderId());
                
                System.out.println("❌ Đã cập nhật order " + notification.getOrderId() + " sang trạng thái PAYMENT_FAILED");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý payment notification: " + e.getMessage());
            e.printStackTrace();
            // Nếu có lỗi, message sẽ được retry hoặc gửi vào DLQ theo config
            throw new RuntimeException("Failed to process payment notification", e);
        }
    }
}
