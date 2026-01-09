package com.example.demo.dto.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UpcomingEventDashboardDto {
    private long events_this_month;
    private long events_last_month;
    private long total_upcoming_events;

    public UpcomingEventDashboardDto(long events_this_month, long events_last_month, long total_upcoming_events) {
        this.events_this_month = events_this_month;
        this.events_last_month = events_last_month;
        this.total_upcoming_events = total_upcoming_events;
    }
}
