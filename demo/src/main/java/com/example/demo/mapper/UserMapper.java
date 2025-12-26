package com.example.demo.mapper;

import com.example.demo.dto.user.CreateUserRequest;
import com.example.demo.dto.user.Profile;
import com.example.demo.dto.user.UpdateUserDTO;
import com.example.demo.dto.user.UsersDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enums.Role;
import com.example.demo.entity.enums.Status;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "username")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", expression = "java(com.example.demo.entity.enums.Role.USER)")
    @Mapping(target = "status", expression = "java(com.example.demo.entity.enums.Status.ACTIVE)")
    UserEntity toEntity(CreateUserRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CreateUserRequest dto, @MappingTarget UserEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateInfo(UpdateUserDTO dto, @MappingTarget UserEntity entity);

    UpdateUserDTO toDto(UserEntity entity);

    Profile toProfile(UserEntity entity);

    List<UsersDTO> toUsersDto(List<UserEntity> entity);
}
