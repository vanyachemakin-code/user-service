package userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.entity.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserEntity toEntity(UserRequestDto userCreateDto);
    UserResponseDto toDto(UserEntity userEntity);
}
