package com.example.demo.service;

import com.example.demo.dto.order.CreateOrderDTO;
import com.example.demo.dto.order.OrderResponse;
import com.example.demo.entity.OrderEntity;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderDTO orderDTO);
    OrderEntity updateOrder(Long orderId, int resultCode);
    void updateOrderPaymentSuccess(Long orderId, String transId);
    void updateOrderPaymentFailed(Long orderId);
    List<OrderResponse> getOrdersByUserId(Long userId);
}
