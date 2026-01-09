package com.example.demo.dto.order;


import lombok.Data;

@Data
public class OrderDashboardDTO {
    private long revenue_this_month;
    private long total_revenue;
    private long revenue_last_month;

    public OrderDashboardDTO(long total_revenue, long revenue_this_month, long revenue_last_month) {
        this.total_revenue = total_revenue;
        this.revenue_this_month = revenue_this_month;
        this.revenue_last_month = revenue_last_month;
    }
}