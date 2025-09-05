package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {
    @InjectMocks
    private UserMapper userMapper;

    @Test
    void shouldMapUserEntityToDto() {
        User user = new User("test fullname", "test", "password123", Role.USER);
        user.setId(1L);

        UserDto dto = userMapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fullname()).isEqualTo("test fullname");
        assertThat(dto.username()).isEqualTo("test");
    }

    @Test
    void shouldMapUserEntityToDtoWithAdminRole() {
        User user = new User("admin fullname", "admin", "adminpassword", Role.ADMIN);
        user.setId(999L);

        UserDto dto = userMapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(999L);
        assertThat(dto.fullname()).isEqualTo("admin fullname");
        assertThat(dto.username()).isEqualTo("admin");
    }

    @Test
    void shouldMapUserEntityToDtoWithNullId() {
        User user = new User("user fullname 2", "user 2", "newpassword", Role.USER);

        UserDto dto = userMapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNull();
        assertThat(dto.fullname()).isEqualTo("user fullname 2");
        assertThat(dto.username()).isEqualTo("user 2");
    }

}
