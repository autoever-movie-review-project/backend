package com.movie.domain.recommendation.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.MovieActors;
import com.movie.domain.movie.domain.MovieDirectors;
import com.movie.domain.movie.domain.MovieGenres;
import com.movie.domain.recommendation.dao.RecommendationRepository;
import com.movie.domain.recommendation.domain.Recommendation;
import com.movie.domain.review.constant.ReviewExceptionMessage;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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

    private User getLoginUser() {
        String loginUserEmail = securityUtils.getLoginUserEmail();
        return userRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new ForbiddenException(ReviewExceptionMessage.NO_PERMISSION.getMessage()));
    }

    @Async
    @Override
    public void generateRecommendationsAsync(User user) {
        List<Movie> movies = movieRepository.findTop50ByOrderByPopularityDesc();
        List<Recommendation> recommendations = new ArrayList<>();

        for (Movie movie : movies) {
            float score = calculateRecommendationScore(user, movie);
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

        recommendations.sort((r1, r2) -> Float.compare(r2.getScore(), r1.getScore()));
        recommendationRepository.saveAll(recommendations.stream().limit(10).toList());
    }

    @Override
    public List<Movie> getRecommendations(User user) {
        List<Recommendation> recommendations = recommendationRepository.findByUserOrderByScoreDesc(user);
        return recommendations.stream()
                .map(Recommendation::getMovie)
                .toList();
    }

    private float calculateRecommendationScore(User user, Movie movie) {
        float score = 0.0f;

        // 1. 배우 선호도
        for (MovieActors movieActor : movie.getMovieActors()) {
            UserActorPreference actorPreference = userActorPreferenceRepository.findByUserAndActor(user, movieActor.getActor());
            if (actorPreference != null) {
                score += actorPreference.getPreferenceScore() * 1.0;
            }
        }

        // 2. 감독 선호도
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            UserDirectorPreference directorPreference = userDirectorPreferenceRepository.findByUserAndDirector(user, movieDirector.getDirector());
            if (directorPreference != null) {
                score += directorPreference.getPreferenceScore() * 1.5;
            }
        }

        // 3. 장르 선호도
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            UserGenrePreference genrePreference = userGenrePreferenceRepository.findByUserAndGenre(user, movieGenre.getGenre());
            if (genrePreference != null) {
                score += genrePreference.getPreferenceScore() * 0.8;
            }
        }

        // 4. 영화 자체 점수
        score += movie.getPopularity() * 0.5;

        return score;
    }

    @Override
    @Transactional
    public void updatePreferences(Long movieId) {
        User user = getLoginUser();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie ID: " + movieId));

        updateActorPreferences(user, movie);
        updateDirectorPreferences(user, movie);
        updateGenrePreferences(user, movie);
    }

    private void updateActorPreferences(User user, Movie movie) {
        for (MovieActors movieActor : movie.getMovieActors()) {
            UserActorPreference preference = userActorPreferenceRepository.findByUserAndActor(user, movieActor.getActor());
            if (preference != null) {
                preference = UserActorPreference.builder()
                        .user(user)
                        .actor(movieActor.getActor())
                        .preferenceScore(preference.getPreferenceScore() + 5) // 기본 점수
                        .build();
            } else {
                preference = UserActorPreference.builder()
                        .user(user)
                        .actor(movieActor.getActor())
                        .preferenceScore(5)
                        .build();
            }
            userActorPreferenceRepository.save(preference);
        }
    }

    private void updateDirectorPreferences(User user, Movie movie) {
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            UserDirectorPreference preference = userDirectorPreferenceRepository.findByUserAndDirector(user, movieDirector.getDirector());
            if (preference != null) {
                preference = UserDirectorPreference.builder()
                        .user(user)
                        .director(movieDirector.getDirector())
                        .preferenceScore(preference.getPreferenceScore() + 3) // 기본 점수
                        .build();
            } else {
                preference = UserDirectorPreference.builder()
                        .user(user)
                        .director(movieDirector.getDirector())
                        .preferenceScore(3)
                        .build();
            }
            userDirectorPreferenceRepository.save(preference);
        }
    }

    private void updateGenrePreferences(User user, Movie movie) {
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            UserGenrePreference preference = userGenrePreferenceRepository.findByUserAndGenre(user, movieGenre.getGenre());
            if (preference != null) {
                preference = UserGenrePreference.builder()
                        .user(user)
                        .genre(movieGenre.getGenre())
                        .preferenceScore(preference.getPreferenceScore() + 2) // 기본 점수
                        .build();
            } else {
                preference = UserGenrePreference.builder()
                        .user(user)
                        .genre(movieGenre.getGenre())
                        .preferenceScore(2)
                        .build();
            }
            userGenrePreferenceRepository.save(preference);
        }
    }
}
