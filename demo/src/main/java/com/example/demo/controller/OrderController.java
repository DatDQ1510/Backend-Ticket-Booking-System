package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.order.CreateOrderDTO;
import com.example.demo.dto.order.OrderDashboardDTO;
import com.example.demo.dto.order.OrderResponse;
import com.example.demo.dto.user.UserDashboardDTO;
import com.example.demo.payload.ApiResponse;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ApiResponse<OrderResponse> createOrder(@RequestBody CreateOrderDTO orderDTO) {
        OrderResponse createdOrder = orderService.tryKeyLock(orderDTO);
        return ApiResponse.success("Order created successfully", createdOrder);
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        Long userId = UserContext.getCurrentUserId();
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ApiResponse.success("Orders retrieved successfully", orders);
    }

    @GetMapping("/dashboard")
    public ApiResponse<OrderDashboardDTO> getUserDashboardData() {

        OrderDashboardDTO dashboardDTO = new OrderDashboardDTO(
                500000,
                200000,
                100000
        );
        return ApiResponse.success("Get full data user Dashboard", dashboardDTO);
//        return orderService.getRevenueDashboardData();
    }
}
