package com.medconnect.userservice.mapper;

import com.medconnect.userservice.dto.UserRequest;
import com.medconnect.userservice.dto.UserResponse;
import com.medconnect.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {


    UserResponse toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "statut", ignore = true)       // Set by constructor
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "googleSub", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    User toEntity(UserRequest request);
}
