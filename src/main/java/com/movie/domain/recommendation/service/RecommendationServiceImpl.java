package com.movie.domain.recommendation.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.MovieActors;
import com.movie.domain.movie.domain.MovieDirectors;
import com.movie.domain.movie.domain.MovieGenres;
import com.movie.domain.movie.exception.MovieIdNotFoundException;
import com.movie.domain.recommendation.dao.RecommendationRepository;
import com.movie.domain.recommendation.domain.Recommendation;
import com.movie.domain.recommendation.dto.RecommendationResDto;
import com.movie.domain.user.dao.UserActorPreferenceRepository;
import com.movie.domain.user.dao.UserDirectorPreferenceRepository;
import com.movie.domain.user.dao.UserGenrePreferenceRepository;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserActorPreference;
import com.movie.domain.user.domain.UserDirectorPreference;
import com.movie.domain.user.domain.UserGenrePreference;
import com.movie.global.exception.ForbiddenException;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;
import static com.movie.global.constant.ExceptionMessage.NOT_FOUND_LOGIN_USER;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final UserActorPreferenceRepository userActorPreferenceRepository;
    private final UserDirectorPreferenceRepository userDirectorPreferenceRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;
    private final MovieRepository movieRepository;
    private final SecurityUtils securityUtils;

    // 현재 로그인된 사용자 가져오기
    private User getLoginUser() {
        String loginUserEmail = securityUtils.getLoginUserEmail();
        return userRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new ForbiddenException(NOT_FOUND_LOGIN_USER.getMessage()));
    }

    // 모든 사용자에 대해 주기적으로 추천 생성
    @Scheduled(cron = "0 0 0 * * SUN") // 매주 일요일 자정 실행
    public void generateRecommendationsForAllUsers() {
        List<User> users = userRepository.findAll(); // 모든 사용자 조회
        for (User user : users) {
            addRecommendations(user); // 사용자별 추천 생성
        }
    }

    @Override
    public void updateLoginUser(){
        User user = getLoginUser();
        addRecommendations(user);
    }

    // 특정 사용자에 대한 추천 생성
    @Transactional
    @Override
    public void addRecommendations(User user) {
        LocalDate recentDate = LocalDate.now().minusDays(60); // 최근 60일 기준
        List<Movie> movies = movieRepository.findTop100ByPopularityAndLanguage(List.of("en", "ko"), 1000);

        List<Recommendation> recommendations = new ArrayList<>();
        for (Movie movie : movies) {
            float score = calculateRecommendationScore(user, movie, recentDate);
            if (score > 0) {
                recommendations.add(
                        Recommendation.builder()
                                .user(user)
                                .movie(movie)
                                .score(score)
                                .build()
                );
            }
        }

        // 추천 점수 순으로 정렬 후 상위 10개 저장
        recommendations.sort((r1, r2) -> Float.compare(r2.getScore(), r1.getScore()));
        recommendationRepository.deleteByUser_UserId(user.getUserId());
        recommendationRepository.saveAll(recommendations.stream().limit(10).toList());
    }

    @Transactional
    @Override
    public void updatePreferences(Long movieId, double rating) {
        User user = getLoginUser();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieIdNotFoundException(MOVIE_NOT_FOUND.getMessage()));

        double weight = rating / 5.0; // 5점 만점 기준 비율

        updateActorPreferences(user, movie, weight);
        updateDirectorPreferences(user, movie, weight);
        updateGenrePreferences(user, movie, weight);
    }

    private void updateActorPreferences(User user, Movie movie, double weight) {
        for (MovieActors movieActor : movie.getMovieActors()) {
            UserActorPreference preference = userActorPreferenceRepository.findByUserAndActor(user, movieActor.getActor());
            double scoreToAdd = weight * 5.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserActorPreference.builder()
                        .user(user)
                        .actor(movieActor.getActor())
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userActorPreferenceRepository.save(preference);
        }
    }

    private void updateDirectorPreferences(User user, Movie movie, double weight) {
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            UserDirectorPreference preference = userDirectorPreferenceRepository.findByUserAndDirector(user, movieDirector.getDirector());
            double scoreToAdd = weight * 3.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserDirectorPreference.builder()
                        .user(user)
                        .director(movieDirector.getDirector())
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userDirectorPreferenceRepository.save(preference);
        }
    }

    private void updateGenrePreferences(User user, Movie movie, double weight) {
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            UserGenrePreference preference = userGenrePreferenceRepository.findByUserAndGenre(user, movieGenre.getGenre());
            double scoreToAdd = weight * 2.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserGenrePreference.builder()
                        .user(user)
                        .genre(movieGenre.getGenre())
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userGenrePreferenceRepository.save(preference);
        }
    }


    @Transactional
    @Override
    public void deleteRecommendations(Long userId) {
        recommendationRepository.deleteByUser_UserId(userId);
    }

    @Transactional
    @Override
    public List<RecommendationResDto> findRecommendations() {
        User user = getLoginUser();
        List<Recommendation> recommendations = recommendationRepository.findByUserOrderByScoreDesc(user);
        return recommendations.stream()
                .map(recommendation -> RecommendationResDto.entityToResDto(
                        recommendation.getMovie(),
                        recommendation.getUser())
                )
                .toList();
    }

    private float calculateRecommendationScore(User user, Movie movie, LocalDate recentDate) {
        float score = 0.0f;

        // 배우 선호도
        for (MovieActors movieActor : movie.getMovieActors()) {
            UserActorPreference actorPreference = userActorPreferenceRepository.findRecentByUserAndActor(user, movieActor.getActor(), recentDate);
            if (actorPreference != null) {
                score += actorPreference.getPreferenceScore();
            }
        }

        // 감독 선호도
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            UserDirectorPreference directorPreference = userDirectorPreferenceRepository.findRecentByUserAndDirector(user, movieDirector.getDirector(), recentDate);
            if (directorPreference != null) {
                score += directorPreference.getPreferenceScore();
            }
        }

        // 장르 선호도
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            UserGenrePreference genrePreference = userGenrePreferenceRepository.findRecentByUserAndGenre(user, movieGenre.getGenre(), recentDate);
            if (genrePreference != null) {
                score += genrePreference.getPreferenceScore();
            }
        }

        // 영화의 인기도
        score += movie.getPopularity() * 0.5f;

        return score;
    }
}
