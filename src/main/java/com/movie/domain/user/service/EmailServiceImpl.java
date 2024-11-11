package com.movie.domain.user.service;

import com.movie.domain.user.constant.EmailExceptionMessage;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.dto.request.CheckEmailCodeReqDto;
import com.movie.domain.user.dto.request.CheckEmailReqDto;
import com.movie.domain.user.dto.response.CheckResDto;
import com.movie.domain.user.exception.EmailCodeNotFoundException;
import com.movie.domain.user.exception.EmailDuplicatedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.movie.domain.user.constant.EmailExceptionMessage.EMAIL_DUPLICATED;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;
    private Random random;

    /**
     * 인증번호 및 임시 비밀번호 생성
     * 랜덤으로 코드 생성
     */
    @Transactional
    @Override
    public void sendEmailCode(String email) {
        // 임의의 authKey 생성
        if (random == null) random = new Random();
        String authKey = String.valueOf(random.nextInt(888888) + 111111);

        String subject = "Movie-Play 회원가입 인증번호";
        String text = "회원 가입을 위한 인증번호는 " + authKey + "입니다. <br/>";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(email); //메일 수신자 설정
            helper.setSubject(subject); // 메일 제목 설정
            helper.setText(text, true); // HTML이라는 의미로 true.

            log.info("[이메일 발신] 이메일 작성 완료. 발송 시도");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("[이메일 발신] 발신 실패.");
            e.printStackTrace();
        }
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 유효 시간(5분)동안 {email, authKey} 저장
        valueOperations.set(email, authKey, 60 * 5L, TimeUnit.SECONDS);

        log.info("[이메일 발신] 이메일 발신 성공. email : {}, code : {}",email, authKey);
    }

    /**
     * 회원가입 전 이메일 중복검사
     */
    @Transactional
    @Override
    public CheckResDto checkEmailDuplicated(String email) {
        log.info("[이메일 중복 검사] 중복 검사 요청. email : {}", email);
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("[이메일 중복 검사] 이메일 중복.");
            throw new EmailDuplicatedException(EMAIL_DUPLICATED.getMessage());

        }
            log.info("[이메일 중복 검사] 중복 검사 완료.");
            return CheckResDto.builder()
                    .success(true)
                    .build();
    }

    /**
     * 발송된 이메일 인증 코드와, 작성한 인증 코드 일치 체크
     */
    @Transactional
    @Override
    public CheckResDto checkEmailCode(CheckEmailCodeReqDto checkEmailCodeReqDto) {
        log.info("[이메일 인증 코드 유효검사] 검사 요청. email : {}, code : {}", checkEmailCodeReqDto.getEmail(), checkEmailCodeReqDto.getCode());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String originCode = (String) valueOperations.get(checkEmailCodeReqDto.getEmail());
        if (originCode == null)
            throw new EmailCodeNotFoundException(EmailExceptionMessage.EMAIL_CODE_NOT_FOUND.getMessage());
        Boolean result = false;
        if (originCode.equals(checkEmailCodeReqDto.getCode())) {
            result = true;
            // 임시 코드 redis 에서 삭제
            valueOperations.getOperations().delete(checkEmailCodeReqDto.getEmail());
            // 인증 완료 저장
            valueOperations.set(checkEmailCodeReqDto.getEmail(), "verified", 60 * 5L, TimeUnit.SECONDS);
        }

        return CheckResDto.builder()
                // 존재하면 false, 존재하지 않으면 true 반환
                .success(result)
                .build();
    }
}
