package com.movie.domain.user.service;

import com.movie.domain.rank.constant.RankName;
import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.constant.EmailExceptionMessage;
import com.movie.domain.user.constant.UserExceptionMessage;
import com.movie.domain.user.dao.LogoutAccessTokenRedisRepository;
import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.LogoutAccessToken;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.dto.request.LoginReqDto;
import com.movie.domain.user.dto.request.UpdatePasswordReqDto;
import com.movie.domain.user.dto.request.SignUpReqDto;
import com.movie.domain.user.dto.response.LoginResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.domain.user.exception.EmailVerificationException;
import com.movie.domain.user.exception.InvalidSignUpException;
import com.movie.global.jwt.JwtTokenProvider;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository refreshTokenRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RankRepository rankRepository;


    @Override
    public void signUp(SignUpReqDto userInfoReqDto) {
        //이메일 중복 검사
        String email = userInfoReqDto.getEmail();
        log.info("[회원가입] 회원가입 요청. email : {}", email);

        // 회원가입 정보 유효성 확인
        if (!checkSignupInfo(userInfoReqDto)) {
            log.error("[회원가입] 회원가입 정보 유효성 불일치.");
            throw new InvalidSignUpException(UserExceptionMessage.SIGN_UP_NOT_VALID.getMessage());
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 이메일 인증 여부 확인
        String checkResult = (String) valueOperations.get(userInfoReqDto.getEmail());
        if (!"verified".equals(checkResult)) {
            throw new EmailVerificationException(EmailExceptionMessage.EMAIL_CHECK_FAILED.getMessage());
        }
        log.info("[회원가입] 이메일 인증 완료.");

        // 패스워드 암호화
        userInfoReqDto.setPassword(passwordEncoder.encode(userInfoReqDto.getPassword()));
        log.info("[회원가입] 패스워드 암호화 완료.");

        // 기본 Rank (BRONZE) 설정
        Rank bronzeRank = rankRepository.findByRankName(RankName.BRONZE.name())
                .orElseThrow(() -> new RuntimeException("[회원가입] 기본 Rank(BRONZE)를 찾을 수 없습니다."));

        User user = userRepository.save(userInfoReqDto.dtoToEntity(bronzeRank));
        log.info("[회원가입] 회원가입이 완료되었습니다.");
    }

    @Override
    public LoginResDto login(LoginReqDto loginReqDto) {
        TokenInfo tokenInfo = setFirstAuthentication(loginReqDto.getEmail(),
                loginReqDto.getPassword());
        log.info("[유저 로그인] 로그인 요청. {} ", tokenInfo);

        User user = userRepository.findByEmail(loginReqDto.getEmail()).get();
        userRedisService.addRefreshToken(user.getEmail(), tokenInfo.getRefreshToken());
        return LoginResDto.builder()
                .accessToken(tokenInfo.getAccessToken())
                .refreshToken(tokenInfo.getRefreshToken())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public void logout(String accessToken) {
        // 로그아웃 여부 redis에 넣어서 accessToken가 유효한지 확인
        String email = SecurityUtils.getLoginUserEmail();
        long remainMilliSeconds = jwtTokenProvider.getRemainingExpiration(accessToken);
        refreshTokenRepository.deleteById(email);
        logoutAccessTokenRepository.save(LogoutAccessToken.builder()
                .email(email)
                .accessToken(accessToken)
                .expiration(remainMilliSeconds / 1000)
                .build());
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public void deleteUser() {

    }

    @Override
    public void updatePassword(UpdatePasswordReqDto updatePasswordReqDto) {

    }

    @Override
    public UserInfoResDto findUser() {
        return null;
    }

    @Override
    public TokenInfo reissueToken(String refreshToken) {
        return null;
    }

    private boolean isSamePassword(String answerPassword, String comparePassword) {
        if (!StringUtils.hasText(comparePassword)) {
            return false;
        }
        if (!passwordEncoder.matches(comparePassword, answerPassword)) {
            return false;
        }
        return true;
    }

    private Boolean checkSignupInfo(SignUpReqDto userInfoReqDto) {
        if (userInfoReqDto.getEmail() == null || userInfoReqDto.getEmail().equals("") ||
                userInfoReqDto.getPassword() == null || userInfoReqDto.getPassword().equals("") ||
                userInfoReqDto.getNickname() == null || userInfoReqDto.getNickname().equals("")) {
            return false;
        }
        return true;
    }

    private TokenInfo setFirstAuthentication(String email, String password) {
        // 1. email과 password를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        // 2. 검증 진행 - CustomUserDetailsService.loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }
}
