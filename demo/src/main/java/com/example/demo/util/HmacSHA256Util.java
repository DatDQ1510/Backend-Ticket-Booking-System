package com.example.demo.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HmacSHA256Util {

    public static String sign(String data, String secretKey) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] bytes = hmacSHA256.doFinal(data.getBytes());
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }

    public static boolean verifySignature(Map<String, Object> data, String accessKey, String secretKey) {
        try {
            // 1. Tạo chuỗi Raw Data từ Map dữ liệu
            String rawData = buildRawData(data, accessKey);
            System.out.println(">>> Raw Data để hash: " + rawData);
            String moMoSignature = String.valueOf(data.get("signature"));
            // 2. Hash chuỗi đó bằng HMAC-SHA256 với Secret Key
            String mySignature = sign(rawData, secretKey);

            // 3. So sánh
            return mySignature.equals(moMoSignature);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm ghép chuỗi theo định dạng chuẩn của MoMo
    // THỨ TỰ CÁC TRƯỜNG LÀ CỰC KỲ QUAN TRỌNG
    private static String buildRawData(Map<String, Object> data, String accessKey) {
        // Lấy các giá trị ra, ép về String, nếu null thì là ""
        String amount = String.valueOf(data.get("amount"));
        String extraData = String.valueOf(data.get("extraData"));
        String message = String.valueOf(data.get("message"));
        String orderId = String.valueOf(data.get("orderId"));
        String orderInfo = String.valueOf(data.get("orderInfo"));
        String orderType = String.valueOf(data.get("orderType"));
        String partnerCode = String.valueOf(data.get("partnerCode"));
        String payType = String.valueOf(data.get("payType"));
        String requestId = String.valueOf(data.get("requestId"));
        String responseTime = String.valueOf(data.get("responseTime"));
        String resultCode = String.valueOf(data.get("resultCode"));
        String transId = String.valueOf(data.get("transId"));

        // Format chuẩn: key=value&key=value...
        return "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCode +
                "&payType=" + payType +
                "&requestId=" + requestId +
                "&responseTime=" + responseTime +
                "&resultCode=" + resultCode +
                "&transId=" + transId;
    }

}
