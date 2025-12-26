package com.example.demo.controller;

import com.example.demo.config.VNPAYConfig;
import com.example.demo.custom.PaymentService;
import com.example.demo.dto.payment.PaymentDTO;
import com.example.demo.payload.ApiResponse;
import com.example.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPAYConfig vnpayConfig;
    private final PaymentService paymentService;

    @GetMapping("/vn-pay")
    public ApiResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        return ApiResponse.success("Success", paymentService.createVnPayPayment(request));
    }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // ✅ Loại bỏ dấu "+" (VNPay đôi khi thêm vào giá trị số điện thoại, v.v.)
                fields.put(fieldName, fieldValue.replace("+", ""));
            }
        }

        // ✅ Lấy SecureHash từ VNPay gửi về
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // ✅ Tạo chuỗi hash (phải sắp xếp key theo thứ tự tăng dần)
        String hashData = VNPayUtil.getHashData(fields);

        // ✅ Hash lại bằng secretKey
        String calculatedHash = VNPayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);

        // ✅ So sánh chữ ký
        if (calculatedHash.equals(vnp_SecureHash)) {
            String status = request.getParameter("vnp_ResponseCode");
            if ("00".equals(status)) {
                return ApiResponse.success("Payment success",
                        PaymentDTO.VNPayResponse.builder()
                                .code("00")
                                .message("Transaction success")
                                .paymentUrl("")
                                .build());
            } else {
                return ApiResponse.error("Payment failed: " + status, null);
            }
        } else {
            return ApiResponse.error("Invalid signature from VNPay", null);
        }
    }
}
