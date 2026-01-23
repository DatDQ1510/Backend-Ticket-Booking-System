package com.example.demo.mapper;

import com.example.demo.dto.event.EventUpdateDTO;
import com.example.demo.dto.event.CreateEventDTO;
import com.example.demo.dto.event.EventResponse;
import com.example.demo.entity.EventEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    @Mapping(target = "date", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "time", dateFormat = "HH:mm:ss")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "eventStatus", ignore = true)
    @Mapping(target = "pictureUrls", ignore = true)
    @Mapping(target = "subTitle", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "geohash", ignore = true) // Will be recalculated in service
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @BeanMapping(ignoreByDefault = false) // vẫn map các field không null
    void updateEventFromDto(EventUpdateDTO dto, @MappingTarget EventEntity entity);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    EventResponse toResponse(EventEntity entity);

    List<EventResponse> toResponseList(List<EventEntity> entities);

    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "eventStatus", ignore = true)
    @Mapping(target = "pictureUrls", ignore = true)
    @Mapping(target = "subTitle", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "geohash", ignore = true) // Will be calculated in service
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    EventEntity toEntity(CreateEventDTO dto);
}
