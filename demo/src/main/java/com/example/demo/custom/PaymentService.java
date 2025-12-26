package com.example.demo.custom;

import com.example.demo.config.VNPAYConfig;
import com.example.demo.dto.payment.PaymentDTO;
import com.example.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPAYConfig vnPayConfig;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = Long.parseLong(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");

        // ✅ Lấy config gốc
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        // ✅ Làm sạch dữ liệu tránh ký tự "+"
        vnpParamsMap.replaceAll((k, v) -> v != null ? v.replace("+", "") : v);

        // ✅ Bước 1: tạo chuỗi hash (KHÔNG encode)
        String hashData = VNPayUtil.getHashData(vnpParamsMap);

        // ✅ Bước 2: tạo chữ ký
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        // ✅ Bước 3: tạo URL thật (CÓ encode và thay '+' bằng '%20')
        String queryUrl = VNPayUtil.getQueryUrl(vnpParamsMap)
                .replace("+", "%20");
        queryUrl += "&vnp_SecureHash=" + URLEncoder.encode(vnpSecureHash, StandardCharsets.UTF_8);

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .code("00")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }
}
