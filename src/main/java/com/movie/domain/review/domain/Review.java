package com.movie.domain.review.domain;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int likesCount;
    private int rating;

}
