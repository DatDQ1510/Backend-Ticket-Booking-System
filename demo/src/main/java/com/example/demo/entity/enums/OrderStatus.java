package com.example.demo.entity.enums;

public enum OrderStatus {
    PENDING,          // vừa tạo đơn
    WAITING_PAYMENT,  // đã tạo link thanh toán Momo
    PAID,             // đã thanh toán (sau callback)
    PAYMENT_FAILED    // thanh toán thất bại
}
