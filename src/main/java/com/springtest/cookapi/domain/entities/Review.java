package com.springtest.cookapi.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String reviewContent;

    @Column(nullable = false)
    @Min(1)
    @Max(10)
    private Integer rating;

    @ManyToOne()
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;
}
