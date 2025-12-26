package com.example.demo.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDTO {
    private Long eventId;
    private String title;
    private String location;
    private String tag;
    private String mainPictureUrl;
    private String subTitle;
}
