package com.example.demo.mapper;

import com.example.demo.dto.PictureDTO;
import com.example.demo.entity.PictureEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PictureMapper {
    
    // MapStruct will automatically map isMain -> main
    PictureDTO toDTO(PictureEntity entity);
    
    List<PictureDTO> toDTOList(List<PictureEntity> entities);
    
    @Mapping(target = "pictureId", ignore = true)
    @Mapping(target = "event", ignore = true)
    // MapStruct will automatically map main -> isMain
    PictureEntity toEntity(PictureDTO dto);
}

