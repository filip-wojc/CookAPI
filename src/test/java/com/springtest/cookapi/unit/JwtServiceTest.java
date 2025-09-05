package com.springtest.cookapi.unit;

import com.springtest.cookapi.infrastructure.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "testsecretkeyfortestingJIAOSJDOIASJDOASIJDOIASJDASOJSADOOJSADIO");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void privateMethodGenerateTokenShouldReturnValidToken() throws Exception {
        Method generateToken = JwtService.class.getDeclaredMethod("generateToken",
                Authentication.class, long.class, Map.class);
        generateToken.setAccessible(true);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test_user");

        Map<String, String> claims = Map.of(
                "userId", "1",
                "username", "test_user",
                "tokenType", "access"
        );

        String token = (String) generateToken.invoke(jwtService, authentication, 3600000L, claims);

        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsernameFromToken(token)).isEqualTo("test_user");
        assertThat(jwtService.extractUserIdFromToken(token)).isEqualTo(1L);
        assertThat(jwtService.isValidToken(token, userDetails)).isTrue();
    }

    @Test
    void privateMethodGenerateTokenShouldReturnNotValidToken() throws Exception {
        Method generateToken = JwtService.class.getDeclaredMethod("generateToken",
                Authentication.class, long.class, Map.class);
        generateToken.setAccessible(true);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("falseUser");

        Map<String, String> claims = Map.of(
                "userId", "1",
                "username", "test_user",
                "tokenType", "access"
        );

        String token = (String) generateToken.invoke(jwtService, authentication, 3600000L, claims);

        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different_user");

        assertThat(token).isNotNull();
        assertThat(jwtService.isValidToken(token, differentUser)).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        Method generateToken = JwtService.class.getDeclaredMethod("generateToken",
                Authentication.class, long.class, Map.class);
        generateToken.setAccessible(true);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test_user");

        String token = (String) generateToken.invoke(jwtService, authentication, 1L, Map.of()); // 1ms

        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isValidToken(token, userDetails);
        });
    }

}