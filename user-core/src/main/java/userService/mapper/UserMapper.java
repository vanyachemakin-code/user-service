package userService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import userService.dto.UserRequestDto;
import userService.dto.UserResponseDto;
import userService.entity.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserEntity toEntity(UserRequestDto userCreateDto);

    UserResponseDto toDto(UserEntity userEntity);
}
