package com.springtest.cookapi.domain.mappers;

import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toUserDto (User user) {
        return new UserDto(
                user.getId(),
                user.getFullname(),
                user.getUsername()
        );
    }
}
