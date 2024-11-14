package com.movie.domain.movie.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer genreId;

    private Integer tmdbGenreId;

    @Column(nullable = false, unique = true)
    private String genre;

    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenres> movieGenres;

    @Builder
    public Genre(Integer tmdbGenreId, String genre) {
        this.tmdbGenreId = tmdbGenreId;
        this.genre = genre;
    }
}
