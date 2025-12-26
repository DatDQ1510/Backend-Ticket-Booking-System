package com.example.demo.mapper;

import com.example.demo.dto.seat.SeatDTO;
import com.example.demo.entity.SeatEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    List<SeatDTO> toUpdatedList(List<SeatEntity> seatEntities);
}
