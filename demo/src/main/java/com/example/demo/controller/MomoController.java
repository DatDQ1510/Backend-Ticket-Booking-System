    package com.example.demo.controller;

    import com.example.demo.config.RabbitMQConfig;
    import com.example.demo.dto.PaymentNotificationDTO;
    import com.example.demo.service.impl.MomoService;
    import com.example.demo.service.OrderService;
    import com.example.demo.util.HmacSHA256Util;
    import lombok.RequiredArgsConstructor;
    import org.springframework.amqp.rabbit.core.RabbitTemplate;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.Map;

    @RestController
    @RequestMapping("/api/momo")
    @RequiredArgsConstructor
    public class MomoController {

        private final MomoService momoService;
        private final RabbitTemplate rabbitTemplate;

        @Value("${momo.accessKey}")
        private String accessKey;

        @Value("${momo.secretKey}")
        private String secretKey;

        @PostMapping("/create")
        public Map<String, Object> createPayment(@RequestParam long amount,
                                                 @RequestParam String orderId,
                                                 @RequestParam String orderInfo
                                                 ) throws Exception {
            // implement tạo payment
            return momoService.createPayment(amount, orderId, orderInfo);
        }

        @GetMapping("/return")
        public String handleReturn(@RequestParam Map<String, String> params) {
            System.out.println(">>> [RETURN] Thanh toán qua trình duyệt:");
            params.forEach((k, v) -> System.out.println(k + " = " + v));

            // Hiển thị kết quả cho người dùng
            if ("0".equals(params.get("resultCode"))) {
                return "Thanh toán thành công! 3123123123123123123123123123";
            } else {
                return "Thanh toán thất bại!";
            }
        }

        @PostMapping("/notify")
        public ResponseEntity<String> handleNotify(@RequestBody Map<String, Object> body) {
            System.out.println("✅ Đinh Quốc Đat - 2050531200254");
            System.out.println(">>> [NOTIFY] Callback từ MoMo server:");
            body.forEach((k, v) -> System.out.println(k + " = " + v));
            
            try {
                Object resultCode = body.get("resultCode");
                
                // Verify signature trước
                boolean isValid = HmacSHA256Util.verifySignature(body, accessKey, secretKey);
                
                if (!isValid) {
                    System.out.println("❌ Chữ ký không hợp lệ!");
                    return ResponseEntity.badRequest().body("Invalid signature");
                } else {
                    System.out.println("✅ Chữ ký hợp lệ!");
                }
                
                // Lấy thông tin từ callback
                Object orderId = body.get("orderId");
                Object transId = body.get("transId");
                Object amount = body.get("amount");
                Object orderInfo = body.get("orderInfo");
                Object requestId = body.get("requestId");
                Object signature = body.get("signature");
                Object extraData = body.get("extraData");
                
                // Extract orderId gốc từ momoOrderId (format: "61_1768409815618" -> "61")
                String momoOrderId = orderId.toString();
                Long originalOrderId = Long.parseLong(momoOrderId.split("_")[0]);
                
                // Tạo DTO để gửi vào RabbitMQ (cả thành công và thất bại)
                PaymentNotificationDTO notification = new PaymentNotificationDTO();
                notification.setOrderId(originalOrderId);
                notification.setResultCode(Integer.parseInt(resultCode.toString()));
                notification.setTransId(transId != null ? transId.toString() : null);
                notification.setAmount(amount != null ? Long.parseLong(amount.toString()) : null);
                notification.setOrderInfo(orderInfo != null ? orderInfo.toString() : null);
                notification.setPaymentType("MOMO");
                notification.setRequestId(requestId != null ? requestId.toString() : null);
                notification.setSignature(signature != null ? signature.toString() : null);
                notification.setExtraData(extraData != null ? extraData.toString() : null);

                // Gửi message vào RabbitMQ để xử lý async (cả success và failed)
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PAYMENT_EXCHANGE,
                        "",  // Routing key rỗng vì dùng FanoutExchange
                        notification
                );

                if (notification.getResultCode() == 0) {
                    System.out.println("✅ Thanh toán thành công. Đã gửi notification vào RabbitMQ cho orderId: " + originalOrderId);
                } else {
                    System.out.println("❌ Thanh toán thất bại (resultCode=" + notification.getResultCode() + "). Đã gửi notification vào RabbitMQ cho orderId: " + originalOrderId);
                }

                // Trả về OK ngay lập tức cho MoMo (cả success và failed đều return OK)
                return ResponseEntity.ok("OK");

            } catch (Exception e) {
                System.err.println("❌ Lỗi khi xử lý notify từ MoMo: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Internal Error");
            }
        }
    }
