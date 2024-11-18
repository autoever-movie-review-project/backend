package com.movie.domain.movie.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"directorName", "birthdate"})})
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long directorId;

    private Long tmdbDirectorId;
    private String directorName;
    private LocalDate birthDate;

    @OneToMany(mappedBy = "director", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDirectors> movieDirectors = new ArrayList<>();

    @Builder
    public Director(Long tmdbDirectorId, String directorName, LocalDate birthDate) {
        this.tmdbDirectorId = tmdbDirectorId;
        this.directorName = directorName;
        this.birthDate = birthDate;
    }
}
