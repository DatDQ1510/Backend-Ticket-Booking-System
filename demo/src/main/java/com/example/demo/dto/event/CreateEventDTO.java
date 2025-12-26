package com.example.demo.dto.event;

import com.example.demo.dto.PictureDTO;
import com.example.demo.entity.PictureEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventDTO {
    private String title;
    private String description;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private String eventType;
    private List<PictureDTO> pictures;
    private String longitude;
    private String latitude;

}
