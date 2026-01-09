package com.example.demo.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class EventDashboardDto {
    private long total_events;
    private long events_this_month;
    private long events_last_month;

    public EventDashboardDto(long total_events, long events_this_month, long events_last_month) {
        this.total_events = total_events;
        this.events_this_month = events_this_month;
        this.events_last_month = events_last_month;
    }
}
