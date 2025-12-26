package com.example.demo.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String payType;
    private Long amount;
}