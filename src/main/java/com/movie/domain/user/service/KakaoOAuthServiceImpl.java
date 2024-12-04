package com.movie.domain.user.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.constant.UserType;
import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.global.config.KakaoOAuthConfig;
import com.movie.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthServiceImpl implements KakaoOAuthService {

    private final KakaoOAuthConfig kakaoOAuthConfig;
    private final RestTemplate kakaoRestTemplate;
    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RankRepository rankRepository;

    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;

    @Transactional
    @Override
    public AuthenticatedResDto processKakaoLogin(String code) {
        log.info("[카카오 로그인] 카카오 로그인 코드: {}", code);

        String kakaoAccessToken = getKakaoAccessToken(code);
        log.info("[카카오 로그인] Access Token 가져오기 완료: {}", kakaoAccessToken);

        JsonObject userInfo = getKakaoUserInfo(kakaoAccessToken);
        log.debug("[카카오 로그인] 사용자 정보 (Raw): {}", userInfo);

        if (userInfo == null || !userInfo.has("kakao_account")) {
            throw new RuntimeException("카카오 사용자 정보 가져오기 실패");
        }

        JsonObject kakaoAccount = userInfo.get("kakao_account").getAsJsonObject();
        if (!kakaoAccount.has("email")) {
            throw new RuntimeException("카카오 사용자 이메일 정보가 존재하지 않음");
        }

        String email = kakaoAccount.get("email").getAsString();
        String nickname = extractNickname(kakaoAccount);
        String profile = extractProfileImage(kakaoAccount);

        User user = findOrCreateUser(email, nickname, profile);
        log.info("[카카오 로그인] 사용자 처리 완료: {}", user.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserType().name()))
        );

        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        saveRefreshTokenInRedis(user.getEmail(), tokenInfo.getRefreshToken());

        return AuthenticatedResDto.builder()
                .userInfo(UserInfoResDto.entityToResDto(user))
                .tokenInfo(tokenInfo)
                .build();
    }

    private String getKakaoAccessToken(String code) {
        String tokenUrl = kakaoOAuthConfig.getTokenUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoOAuthConfig.getClientId());
        body.add("redirect_uri", kakaoOAuthConfig.getRedirectUri());
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String response = kakaoRestTemplate.postForObject(tokenUrl, request, String.class);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            return jsonResponse.get("access_token").getAsString();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("카카오 Access Token 요청 실패: " + e.getMessage());
        }
    }

    private JsonObject getKakaoUserInfo(String kakaoAccessToken) {
        String userInfoUrl = kakaoOAuthConfig.getUserInfoUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            String response = kakaoRestTemplate.postForObject(userInfoUrl, request, String.class);
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("카카오 사용자 정보 요청 실패: " + e.getMessage());
        }
    }

    private User findOrCreateUser(String email, String nickname, String profile) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, nickname, profile));
    }

    private User registerNewUser(String email, String nickname, String profile) {
        log.info("[카카오 회원가입] 신규 회원 등록 시도: 이메일 = {}, 닉네임 = {}", email, nickname);

        Integer defaultPoints = 0;
        Rank defaultRank = rankRepository.findByPointsBetweenStartAndEnd(defaultPoints)
                .orElseThrow(() -> new RuntimeException("[회원가입] 기본 Rank를 찾을 수 없습니다."));

        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .profile(profile)
                .password("")
                .userType(UserType.ROLE_SOCIAL)
                .rank(defaultRank)
                .points(defaultPoints)
                .build();

        log.info("[카카오 회원가입] 저장 전 사용자 객체: {}", newUser);

        try {
            User savedUser = userRepository.save(newUser);
            log.info("[카카오 회원가입] 저장된 사용자 ID: {}, 이메일: {}", savedUser.getUserId(), savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            log.error("[카카오 회원가입] 사용자 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("사용자 저장 실패", e);
        }
    }

    private void saveRefreshTokenInRedis(String email, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .email(email)
                .refreshToken(refreshToken)
                .expiration(REFRESH_TOKEN_EXPIRED_IN)
                .build();

        refreshTokenRedisRepository.save(token);
    }

    private String extractNickname(JsonObject kakaoAccount) {
        JsonObject profileData = kakaoAccount.has("profile") && !kakaoAccount.get("profile").isJsonNull()
                ? kakaoAccount.get("profile").getAsJsonObject()
                : null;

        return (profileData != null && profileData.has("nickname") && !profileData.get("nickname").isJsonNull())
                ? profileData.get("nickname").getAsString()
                : "익명";
    }

    private String extractProfileImage(JsonObject kakaoAccount) {
        JsonObject profileData = kakaoAccount.has("profile") && !kakaoAccount.get("profile").isJsonNull()
                ? kakaoAccount.get("profile").getAsJsonObject()
                : null;

        return (profileData != null && profileData.has("profile_image_url") && !profileData.get("profile_image_url").isJsonNull())
                ? profileData.get("profile_image_url").getAsString()
                : null;
    }
}
