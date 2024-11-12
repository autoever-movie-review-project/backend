package com.movie.domain.user.service;

import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.constant.EmailExceptionMessage;
import com.movie.domain.user.constant.UserExceptionMessage;
import com.movie.domain.user.dao.LogoutAccessTokenRedisRepository;
import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.LogoutAccessToken;
import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.dto.request.LoginReqDto;
import com.movie.domain.user.dto.request.SignUpReqDto;
import com.movie.domain.user.dto.request.UpdatePasswordReqDto;
import com.movie.domain.user.dto.request.UpdateUserReqDto;
import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.domain.user.exception.EmailVerificationException;
import com.movie.domain.user.exception.InvalidSignUpException;
import com.movie.domain.user.exception.UserNotFoundException;
import com.movie.global.exception.NotFoundException;
import com.movie.global.exception.TokenException;
import com.movie.global.jwt.JwtTokenProvider;
import com.movie.global.jwt.constant.JwtHeaderUtil;
import com.movie.global.security.util.SecurityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.movie.domain.user.constant.UserExceptionMessage.*;

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

    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;

    /**
     * 사용자 회원가입
     */
    @Override
    public void signUp(SignUpReqDto userInfoReqDto) {
        // 이메일 중복 검사
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

        // 기본 포인트를 기준으로 Rank 찾기
        Integer defaultPoints = 0;
        Rank defaultRank = rankRepository.findByPointsBetweenStartAndEnd(defaultPoints)
                .orElseThrow(() -> new RuntimeException("[회원가입] 기본 Rank를 찾을 수 없습니다."));

        User user = userRepository.save(userInfoReqDto.dtoToEntity(defaultRank));
        log.info("[회원가입] 회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 처리 후 Access 및 Refresh Token 발급
     */
    @Override
    public AuthenticatedResDto login(LoginReqDto loginReqDto) {
        TokenInfo tokenInfo = setFirstAuthentication(loginReqDto.getEmail(),
                loginReqDto.getPassword());
        log.info("[유저 로그인] 로그인 요청. {} ", tokenInfo);

        User user = userRepository.findByEmail(loginReqDto.getEmail()).get();

        userRedisService.addRefreshToken(user.getEmail(), tokenInfo.getRefreshToken());

        return AuthenticatedResDto.entityToResDto(tokenInfo, user);
    }

    /**
     * 로그아웃 시 Redis에 로그아웃 처리 및 Token 무효화
     */
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

        log.info("[로그아웃] 로그아웃 완료.");
    }

    /**
     * 사용자 프로필 정보 업데이트
     */
    @Override
    public UserInfoResDto updateUser(UpdateUserReqDto updateUserReqDto) {
        String email = SecurityUtils.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidSignUpException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));

        user.updateUser(updateUserReqDto);
        userRepository.save(user);

        log.info("[내 정보 수정] 정보 수정 완료.");

        return UserInfoResDto.entityToResDto(user);
    }

    /**
     * 사용자 계정 삭제, 연관된 Token 제거
     */
    @Override
    public void deleteUser() {
        String email = SecurityUtils.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidSignUpException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));

        log.debug("[회원 탈퇴] 탈퇴 요청. 로그인 유저 : {}", user.getEmail());

        userRedisService.deleteRefreshToken(user.getEmail());
        userRepository.deleteByEmail(email);
    }

    /**
     * 비밀번호 변경, 일치 여부 확인
     */
    @Override
    public void updatePassword(UpdatePasswordReqDto updatePasswordReqDto) {
        String email = SecurityUtils.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidSignUpException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));

        log.info("[비밀번호 변경] 변경 요청. 로그인 유저 : {}", user.getEmail());

        if (!isSamePassword(user.getPassword(), updatePasswordReqDto.getPassword())) {
            log.error("[비밀번호 수정] 기존 비밀번호 불일치.");
            throw new InvalidSignUpException(UserExceptionMessage.LOGIN_PASSWORD_ERROR.getMessage());
        }

        user.updatePassword(passwordEncoder.encode(updatePasswordReqDto.getPassword()));
        userRepository.save(user);
        log.info("[비밀번호 수정] 비밀번호 수정 완료.");

    }

    /**
     * Access Token에서 Bearer 타입을 제거한 순수 Token 값 반환
     */
    private String parseAccessToken(String accessToken) {
        return accessToken.substring(JwtHeaderUtil.GRANT_TYPE.getValue().length());
    }

    /**
     * 사용자 프로필 정보 조회
     */
    @Override
    public UserInfoResDto findUser() {
        String email = SecurityUtils.getLoginUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));

        log.info("[회원정보 조회] 조회 완료. email : {}", email);
        return UserInfoResDto.entityToResDto(user);
    }

    /**
     * 만료된 Refresh Token으로 새 Access 및 Refresh Token 발급
     */
    @Override
    public TokenInfo reissueToken(String accessToken, String refreshToken) {
        log.info("[토큰 재발급] 토큰 재발급 요청. accessToken : {}, refreshToken : {}", accessToken, refreshToken);

        String email = getEmailFromToken(refreshToken);

        checkRefreshToken(refreshToken, email);

        TokenInfo newTokenInfo = addTokens(email);

        log.info("[토큰 재발급] 토큰 재발급 성공. 새로운 accessToken : {}, 새로운 refreshToken : {}", newTokenInfo.getAccessToken(), newTokenInfo.getRefreshToken());
        return newTokenInfo;
    }

    /**
     * Refresh Token에서 이메일 추출
     */
    private String getEmailFromToken(String refreshToken) {
        try {
            return jwtTokenProvider.getUsernameFromExpiredToken(refreshToken);
        } catch (ExpiredJwtException e) {
            log.error("[토큰 재발급] 리프레시 토큰이 만료되었습니다. 재로그인 필요.");
            throw new TokenException(REFRESH_TOKEN_EXPIRED.getMessage());
        }
    }

    /**
     * Refresh Token의 유효성 확인
     */
    private void checkRefreshToken(String refreshToken, String email) {
        String storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new NotFoundException(REFRESH_TOKEN_NOT_FOUND.getMessage()))
                .getRefreshToken();

        if (!storedToken.equals(refreshToken)) {
            log.error("[토큰 재발급] 토큰 불일치. 재발급 불가.");
            throw new TokenException(TOKEN_MISMATCH.getMessage());
        }
    }

    /**
     * 새로운 Token 생성 후 Redis에 저장
     */
    private TokenInfo addTokens(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
        TokenInfo newTokens = jwtTokenProvider.generateToken(authentication);

        refreshTokenRepository.deleteById(email);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .email(email)
                        .refreshToken(newTokens.getRefreshToken())
                        .expiration(REFRESH_TOKEN_EXPIRED_IN / 1000)
                        .build()
        );

        return newTokens;
    }

    /**
     * 비밀번호 일치 여부 확인
     */
    private boolean isSamePassword(String answerPassword, String comparePassword) {
        if (!StringUtils.hasText(comparePassword)) {
            return false;
        }
        if (!passwordEncoder.matches(comparePassword, answerPassword)) {
            return false;
        }
        return true;
    }

    /**
     * 회원가입 요청의 유효성을 검사
     */
    private Boolean checkSignupInfo(SignUpReqDto userInfoReqDto) {
        return StringUtils.hasText(userInfoReqDto.getEmail())
                && StringUtils.hasText(userInfoReqDto.getPassword());
    }

    /**
     * 인증 후 Access 및 Refresh Token 발급
     */
    private TokenInfo setFirstAuthentication(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("[인증 처리] 인증 성공. email : {}", email);
            return jwtTokenProvider.generateToken(authentication);
        } catch (Exception e) {
            log.error("[인증 실패] email : {}, error: {}", email, e.getMessage());
            throw e;
        }
    }
}
