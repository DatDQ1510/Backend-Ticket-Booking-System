package com.example.demo.dto.payment;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentDTO {
    private String orderId;
    private String requestId;
    private long amount;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType;
}
