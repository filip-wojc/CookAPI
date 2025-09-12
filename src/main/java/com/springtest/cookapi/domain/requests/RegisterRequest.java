package com.springtest.cookapi.domain.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50)
    private String fullname;

    @NotBlank(message = "Username is required")
    private String username;

    @Size(min = 3, message = "Password must be min 3 characters")
    private String password;

}