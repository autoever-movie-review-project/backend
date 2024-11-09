package com.movie.domain.user.dto.request;

import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.constant.UserType;
import com.movie.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

@Data
@Builder
public class SignUpReqDto {
    @Email
    private String email;
    @Length(min = 8, max = 16, message = "비밀번호는 최소 8글자 최대 16글자 입니다.")
    private String password;
    private String nickname;
    private String profile;

    public User dtoToEntity(Rank defaultRank) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .userType(UserType.ROLE_USER)
                .profile(profile)
                .rank(defaultRank)
                .build();
    }
}
