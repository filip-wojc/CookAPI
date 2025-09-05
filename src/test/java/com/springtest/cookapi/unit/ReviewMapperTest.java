package com.springtest.cookapi.unit;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.dtos.user.UserDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.Review;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.Role;
import com.springtest.cookapi.domain.mappers.ReviewMapper;
import com.springtest.cookapi.domain.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewMapperTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReviewMapper reviewMapper;

    @Test
    void shouldMapReviewEntityToDto() {
        User user = new User("test fullname", "test", "password", Role.USER);
        user.setId(1L);

        Recipe recipe = new Recipe();
        recipe.setId(10L);

        Review review = new Review(
                1L,
                "test title",
                "test content",
                5,
                recipe,
                user
        );

        UserDto userDto = new UserDto(1L, "test fullname", "test");

        when(userMapper.toUserDto(user)).thenReturn(userDto);

        ReviewDto dto = reviewMapper.toReviewDto(review);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("test title");
        assertThat(dto.reviewContent()).isEqualTo("test content");
        assertThat(dto.rating()).isEqualTo(5);
        assertThat(dto.author()).isEqualTo(userDto);
        assertThat(dto.recipeId()).isEqualTo(10L);

        verify(userMapper).toUserDto(user);
    }

    @Test
    void shouldMapCreateReviewDtoToEntity() {
        CreateReviewDto dto = new CreateReviewDto(
                "test recipe 2",
                "test content 2",
                4
        );

        Review review = reviewMapper.toReview(dto);

        assertThat(review).isNotNull();
        assertThat(review.getId()).isNull();
        assertThat(review.getTitle()).isEqualTo("test recipe 2");
        assertThat(review.getReviewContent()).isEqualTo("test content 2");
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getUser()).isNull();
        assertThat(review.getRecipe()).isNull();
    }

    @Test
    void shouldMapReviewEntityToDtoWithMinimumRating() {
        User user = new User("test user 2", "test fullname 2", "password", Role.USER);
        user.setId(2L);

        Recipe recipe = new Recipe();
        recipe.setId(20L);

        Review review = new Review(
                2L,
                "test title 3",
                "test content 3",
                1,
                recipe,
                user
        );

        UserDto userDto = new UserDto(2L, "test user 3", "test user 3");

        when(userMapper.toUserDto(user)).thenReturn(userDto);

        ReviewDto dto = reviewMapper.toReviewDto(review);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.title()).isEqualTo("test title 3");
        assertThat(dto.reviewContent()).isEqualTo("test content 3");
        assertThat(dto.rating()).isEqualTo(1);
        assertThat(dto.author()).isEqualTo(userDto);
        assertThat(dto.recipeId()).isEqualTo(20L);

        verify(userMapper).toUserDto(user);
    }
}
