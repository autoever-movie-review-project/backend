package com.movie.domain.user.service;

import com.movie.domain.user.dto.request.CheckEmailCodeReqDto;
import com.movie.domain.user.dto.request.CheckEmailReqDto;
import com.movie.domain.user.dto.response.CheckResDto;

public interface EmailService {
    void sendEmailCode(CheckEmailReqDto checkEmailReqDto); //이메일 인증코드 발송
    CheckResDto checkEmailDuplicated(CheckEmailReqDto checkEmailReqDto); //이메일 중복 검사
    CheckResDto checkEmailCode(CheckEmailCodeReqDto checkEmailCodeReqDto); //이메일 인증코드 유효검사
}
