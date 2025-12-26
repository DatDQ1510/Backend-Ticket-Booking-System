package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private Integer resultCode;
    private String transId;
    private Long amount;
    private String orderInfo;
    private String paymentType;
    
    // Thêm các field khác nếu cần
    private String requestId;
    private String signature;
    private String extraData;
}
