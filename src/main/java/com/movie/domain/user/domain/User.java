package com.movie.domain.user.domain;

import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.constant.UserType;
import com.movie.domain.user.dto.request.UpdateProfileReqDto;
import com.movie.domain.user.dto.request.UpdateUserReqDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 16, nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "varchar(50) default 'ROLE_USER'")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column
    private String profile;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int points = 0;

    @ManyToOne
    @JoinColumn(name = "rank_id")
    private Rank rank;

    @Builder
    public User(String email, String password, String nickname, UserType userType, String profile, int points, Rank rank) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userType = (userType != null) ? userType : UserType.ROLE_USER;
        this.profile = profile;
        this.points = points;
        this.rank = rank;
    }

    public void updateUser(UpdateUserReqDto updateUserReqDto) {
        this.nickname = updateUserReqDto.getNickname();
        this.profile = updateUserReqDto.getProfile();
    }

    public void updateProfile(UpdateProfileReqDto updateProfileReqDtou) {
        this.profile = updateProfileReqDtou.getProfile();
    }

    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }
}
