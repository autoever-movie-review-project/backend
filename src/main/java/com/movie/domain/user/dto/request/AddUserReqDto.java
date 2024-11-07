package com.movie.domain.user.dto.request;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

@Data
@Builder
public class AddUserReqDto {

    @Email
    private String email;

    @Length(min = 8, max = 16, message = "비밀번호는 최소 8글자 최대 16글자 입니다.")
    private String password;

    private String nickname;

    private String uuid;

}
