package com.movie.domain.PointHistory.domain;

import com.movie.domain.user.domain.User;
import com.movie.global.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PointHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_Id")
    private User user;

    private Integer points;
    private String description;
}
