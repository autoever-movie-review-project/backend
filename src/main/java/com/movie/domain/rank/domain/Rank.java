package com.movie.domain.rank.domain;


import com.movie.domain.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "ranks")
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rankId;

    @Column(nullable = false)
    private String rankName;

    private String rankImg;

    @Column(nullable = false)
    private Integer startPoint;

    @Column(nullable = false)
    private Integer endPoint;

    @OneToMany(mappedBy = "rank")
    private List<User> users;

}