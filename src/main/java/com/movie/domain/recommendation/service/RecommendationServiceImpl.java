package com.movie.domain.recommendation.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.MovieActors;
import com.movie.domain.movie.domain.MovieDirectors;
import com.movie.domain.movie.domain.MovieGenres;
import com.movie.domain.recommendation.domain.Recommendation;
import com.movie.domain.recommendation.repository.RecommendationRepository;
import com.movie.domain.user.dao.UserActorPreferenceRepository;
import com.movie.domain.user.dao.UserDirectorPreferenceRepository;
import com.movie.domain.user.dao.UserGenrePreferenceRepository;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserActorPreference;
import com.movie.domain.user.domain.UserDirectorPreference;
import com.movie.domain.user.domain.UserGenrePreference;
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
    private final UserActorPreferenceRepository userActorPreferenceRepository;
    private final UserDirectorPreferenceRepository userDirectorPreferenceRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;
    private final MovieRepository movieRepository;

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

    // 선호도 업데이트
    @Override
    @Transactional
    public void updatePreferencesAfterAction(User user, Movie movie, int weight) {
        // 배우 선호도 업데이트
        for (MovieActors movieActor : movie.getMovieActors()) {
            UserActorPreference preference = userActorPreferenceRepository.findByUserAndActor(user, movieActor.getActor());
            if (preference != null) {
                userActorPreferenceRepository.save(
                        UserActorPreference.builder()
                                .user(user)
                                .actor(movieActor.getActor())
                                .preferenceScore(preference.getPreferenceScore() + weight)
                                .build()
                );
            } else {
                userActorPreferenceRepository.save(
                        UserActorPreference.builder()
                                .user(user)
                                .actor(movieActor.getActor())
                                .preferenceScore(weight)
                                .build()
                );
            }
        }

        // 감독 선호도 업데이트
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            UserDirectorPreference preference = userDirectorPreferenceRepository.findByUserAndDirector(user, movieDirector.getDirector());
            if (preference != null) {
                userDirectorPreferenceRepository.save(
                        UserDirectorPreference.builder()
                                .user(user)
                                .director(movieDirector.getDirector())
                                .preferenceScore(preference.getPreferenceScore() + weight)
                                .build()
                );
            } else {
                userDirectorPreferenceRepository.save(
                        UserDirectorPreference.builder()
                                .user(user)
                                .director(movieDirector.getDirector())
                                .preferenceScore(weight)
                                .build()
                );
            }
        }

        // 장르 선호도 업데이트
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            UserGenrePreference preference = userGenrePreferenceRepository.findByUserAndGenre(user, movieGenre.getGenre());
            if (preference != null) {
                userGenrePreferenceRepository.save(
                        UserGenrePreference.builder()
                                .user(user)
                                .genre(movieGenre.getGenre())
                                .preferenceScore(preference.getPreferenceScore() + weight)
                                .build()
                );
            } else {
                userGenrePreferenceRepository.save(
                        UserGenrePreference.builder()
                                .user(user)
                                .genre(movieGenre.getGenre())
                                .preferenceScore(weight)
                                .build()
                );
            }
        }
    }
}
