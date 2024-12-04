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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"actorName", "birthDate"})})
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actorId;

    private Long tmdbActorId;
    private String actorName;
    private LocalDate birthDate;
    private String actorImg;

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieActors> movieActors = new ArrayList<>();

    @Builder
    public Actor(Long tmdbActorId, String actorName, LocalDate birthDate, String actorImg) {
        this.tmdbActorId = tmdbActorId;
        this.actorName = actorName;
        this.birthDate = birthDate;
        this.actorImg = actorImg;
    }
}
