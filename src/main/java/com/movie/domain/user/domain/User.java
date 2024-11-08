package com.movie.domain.user.domain;

import com.movie.domain.user.constant.UserType;
import com.movie.domain.user.dto.request.UpdateUserReqDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "varchar(50) default 'ROLE_USER'")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Builder
    public User(String email, String password, String nickname, UserType userType) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userType = userType != null ? userType : UserType.ROLE_USER;
    }

    //회원 정보 업데이트
    public void updateUser(UpdateUserReqDto updateUserReqDto) {
        this.nickname = updateUserReqDto.getNickname();
    }

    //비밀번호 변경
    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }
}
