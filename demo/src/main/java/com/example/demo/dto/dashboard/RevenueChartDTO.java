package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Revenue Chart Data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {
    private List<String> labels;  // Dates or periods
    private List<BigDecimal> data;  // Revenue values
    private String period;  // "day", "week", "month"
}
