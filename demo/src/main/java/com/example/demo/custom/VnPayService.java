//package com.example.demo.custom;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Service
//public class VnPayService {
//
//    @Value("${vnpay.tmn-code}")
//    private String tmnCode;
//
//    @Value("${vnpay.hash-secret}")
//    private String hashSecret;
//
//    @Value("${vnpay.payment-url}")
//    private String paymentUrlBase;
//
//    @Value("${vnpay.return-url}")
//    private String returnUrl;
//
//    @Value("${vnpay.ipn-url}")
//    private String ipnUrl;
//
//    public String createPaymentUrl(String orderId, long amount, String description,
//                                   String bankCode, String locale, String clientIp) {
//
//        Map<String, String> params = new HashMap<>();
//        params.put("vnp_Version", "2.1.0");
//        params.put("vnp_Command", "pay");
//        params.put("vnp_TmnCode", tmnCode);
//        params.put("vnp_Amount", String.valueOf(amount * 100));
//        params.put("vnp_CurrCode", "VND");
//        params.put("vnp_TxnRef", orderId);
//        params.put("vnp_OrderInfo", description.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim());
//        params.put("vnp_OrderType", "other");
//        params.put("vnp_Locale", locale != null ? locale : "vn");
//        params.put("vnp_ReturnUrl", returnUrl);
//        params.put("vnp_IpAddr", "127.0.0.1");
//        params.put("vnp_BankCode", bankCode != null && !bankCode.isEmpty() ? bankCode : "NCB");
//
//        String createDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
//                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        params.put("vnp_CreateDate", createDate);
//
//        String expireDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
//                .plusMinutes(15)
//                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        params.put("vnp_ExpireDate", expireDate);
//
//        // ✅ Sắp xếp key tăng dần
//        List<String> fieldNames = new ArrayList<>(params.keySet());
//        Collections.sort(fieldNames);
//
//        // ✅ Tạo hashData và query string KHÔNG dư dấu &
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String key = fieldNames.get(i);
//            String value = params.get(key);
//            if (value != null && !value.isEmpty()) {
//                if (i > 0) {
//                    hashData.append('&');
//                    query.append('&');
//                }
//                hashData.append(key).append('=').append(value);
//                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
//                        .append('=')
//                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
//            }
//        }
//
//        // ✅ Sinh Secure Hash
//        String secureHash = hmacSHA512(hashSecret, hashData.toString());
//        query.append("&vnp_SecureHashType=HMACSHA512&vnp_SecureHash=").append(secureHash);
//
//        System.out.println("===== VNPAY HASH DATA =====");
//        System.out.println(hashData);
//        System.out.println("===== SECURE HASH =====");
//        System.out.println(secureHash);
//
//        return paymentUrlBase + "?" + query;
//    }
//
//    public boolean handleReturn(HttpServletRequest request) {
//        Map<String, String> fields = getVnpayResponse(request);
//        String receivedHash = fields.remove("vnp_SecureHash");
//        fields.remove("vnp_SecureHashType");
//
//        String calculatedHash = hmacSHA512(hashSecret, buildHashData(fields));
//
//        System.out.println("Received hash: " + receivedHash);
//        System.out.println("Calculated hash: " + calculatedHash);
//
//        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash)
//                && "00".equals(fields.get("vnp_ResponseCode"));
//    }
//
//    public boolean handleIpn(HttpServletRequest request) {
//        Map<String, String> fields = getVnpayResponse(request);
//        String receivedHash = fields.remove("vnp_SecureHash");
//        fields.remove("vnp_SecureHashType");
//
//        String calculatedHash = hmacSHA512(hashSecret, buildHashData(fields));
//
//        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash)
//                && "00".equals(fields.get("vnp_ResponseCode"));
//    }
//
//    private Map<String, String> getVnpayResponse(HttpServletRequest request) {
//        Map<String, String> fields = new HashMap<>();
//        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
//            String fieldName = params.nextElement();
//            String fieldValue = request.getParameter(fieldName);
//            if (fieldValue != null && !fieldValue.isEmpty()) {
//                fields.put(fieldName, fieldValue);
//            }
//        }
//        return fields;
//    }
//
//    private String buildHashData(Map<String, String> fields) {
//        List<String> fieldNames = new ArrayList<>(fields.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String key = fieldNames.get(i);
//            String value = fields.get(key);
//            if (i > 0) sb.append('&');
//            sb.append(key).append('=').append(value);
//        }
//        return sb.toString();
//    }
//
//    private String hmacSHA512(String key, String data) {
//        try {
//            Mac hmac = Mac.getInstance("HmacSHA512");
//            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
//            hmac.init(secretKey);
//            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hash = new StringBuilder();
//            for (byte b : bytes) {
//                hash.append(String.format("%02x", b));
//            }
//            return hash.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Cannot generate HMAC SHA512", e);
//        }
//    }
//}
