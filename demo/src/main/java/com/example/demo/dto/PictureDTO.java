package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PictureDTO {
    private String pictureUrl;
    private String publicId;
    private boolean isMain; // Renamed from isMain to avoid Lombok confusion
}
