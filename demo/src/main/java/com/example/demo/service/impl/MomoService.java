package com.example.demo.service.impl;

import com.example.demo.util.HmacSHA256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MomoService {

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    public Map<String, Object> createPayment(long amount, String orderId, String orderInfo) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String extraData = ""; // phải có field, để trống cũng được
        String momoOrderId = orderId + "_" + System.currentTimeMillis();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", String.valueOf(amount));
        body.put("orderId", orderId); // Use original orderId, not momoOrderId
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl); // đúng tên field theo tài liệu MoMo
        body.put("ipnUrl", notifyUrl);      // đúng tên field theo tài liệu MoMo
        body.put("extraData", extraData);
        body.put("requestType", "captureWallet");

        // ✅ raw hash đúng thứ tự key theo tài liệu MoMo
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId + // Use original orderId
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        String signature = HmacSHA256Util.sign(rawHash, secretKey);
        body.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, entity, Map.class);
        return response.getBody();
    }
}
