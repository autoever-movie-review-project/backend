package com.movie.domain.movie.domain;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Entity
@Table
@Getter
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mainImage;
}
