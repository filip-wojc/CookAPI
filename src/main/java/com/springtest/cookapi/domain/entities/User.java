package com.springtest.cookapi.domain.entities;

import com.springtest.cookapi.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50)
    @Column(name = "full_name", nullable = false)
    private String fullname;

    @NotBlank(message = "Username is required")
    @Column(nullable = false)
    private String username;

    @Size(min = 3, message = "Password must be min 3 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe> recipes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public User(String fullname, String username, String password, Role role) {
        this.fullname = fullname;
        this.username = username;
        this.role = role;
        this.password = password;
    }
}