package com.movie.domain.user.service;

import com.movie.domain.user.dao.LogoutAccessTokenRedisRepository;
import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.dto.request.CheckEmailCodeReqDto;
import com.movie.domain.user.dto.request.CheckEmailReqDto;
import com.movie.domain.user.dto.response.CheckResDto;
import com.movie.domain.user.exception.EmailDuplicatedException;
import com.movie.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.movie.domain.user.constant.UserExceptionMessage.EMAIL_DUPLICATED;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;
    private Random random;

    @Override
    public void sendEmailCode(CheckEmailReqDto checkEmailReqDto) {
        // 임의의 authKey 생성
        if (random == null) random = new Random();
        String authKey = String.valueOf(random.nextInt(888888) + 111111);

        String subject = "Movie-Play 회원가입 인증번호";
        String text = "회원 가입을 위한 인증번호는 " + authKey + "입니다. <br/>";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(checkEmailReqDto.getEmail());
            helper.setSubject(subject);
            helper.setText(text, true); // HTML이라는 의미로 true.
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 유효 시간(5분)동안 {email, authKey} 저장
        valueOperations.set(checkEmailReqDto.getEmail(), authKey, 60 * 5L, TimeUnit.SECONDS);
    }

    @Override
    public CheckResDto checkEmailDuplicated(CheckEmailReqDto checkEmailReqDto) {
        if (userRepository.findByEmail(checkEmailReqDto.getEmail()).isPresent()) {
            throw new EmailDuplicatedException(EMAIL_DUPLICATED.getMessage());
        }
        return CheckResDto.builder()
                .success(true)
                .build();
    }

    @Override
    public CheckResDto checkEmailCode(CheckEmailCodeReqDto checkEmailCodeReqDto) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String originCode = (String) valueOperations.get(checkEmailCodeReqDto.getEmail());
        if (originCode == null)
            throw new NotFoundException("해당 이메일로 유효한 인증 코드가 존재하지 않습니다");
        Boolean result = false;
        if (originCode.equals(checkEmailCodeReqDto.getCode())) {
            result = true;
            // 코드를 확인했으므로 redis 에서 삭제
            valueOperations.getOperations().delete(checkEmailCodeReqDto.getEmail());
            valueOperations.set(checkEmailCodeReqDto.getEmail(), "이메일 인증 완료", 60 * 5L, TimeUnit.SECONDS);
        }
        return CheckResDto.builder()
                // 존재하면 false, 존재하지 않으면 true 반환
                .success(result)
                .build();
    }
}
