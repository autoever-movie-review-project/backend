package com.movie.domain.recommendation.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.dao.PopularMovieRedisRepository;
import com.movie.domain.movie.domain.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;
import static com.movie.global.constant.ExceptionMessage.NOT_FOUND_LOGIN_USER;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final PopularMovieRedisRepository redisRepository;
    private final UserActorPreferenceRepository userActorPreferenceRepository;
    private final UserDirectorPreferenceRepository userDirectorPreferenceRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;
    private final MovieRepository movieRepository;
    private final SecurityUtils securityUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private User getLoginUser() {
        String loginUserEmail = securityUtils.getLoginUserEmail();
        return userRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new ForbiddenException(NOT_FOUND_LOGIN_USER.getMessage()));
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    public void generateRecommendationsForAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            addRecommendations(user);
        }
    }

    //레디스에 있으면 가져오기
    @Transactional
    public List<PopularMovieInfo.MovieDetail> getDailyPopularMovies() {
        String targetDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "popularMovieInfo:" + targetDate;

        Optional<PopularMovieInfo> cachedPopularMovies = redisRepository.findById(targetDate);
        if (cachedPopularMovies.isPresent()) {
            return cachedPopularMovies.get().getMovies();
        }
        return fetchAndSavePopularMovies(targetDate);
    }

    //레디스 저장
    private List<PopularMovieInfo.MovieDetail> fetchAndSavePopularMovies(String targetDate) {
        List<Movie> movies = movieRepository.findTop50ByPopularityAndLanguage(List.of("en", "ko"), 1000);

        List<PopularMovieInfo.MovieDetail> movieDetails = movies.stream()
                .map(movie -> PopularMovieInfo.MovieDetail.builder()
                        .movieId(movie.getMovieId())
                        .popularityScore(0)
                        .genreIds(movie.getMovieGenres().stream()
                                .map(genre -> genre.getGenre().getGenreId())
                                .toList())
                        .actorIds(movie.getMovieActors().stream()
                                .map(actor -> actor.getActor().getActorId())
                                .toList())
                        .directorIds(movie.getMovieDirectors().stream()
                                .map(director -> director.getDirector().getDirectorId())
                                .toList())
                        .build())
                .toList();

        PopularMovieInfo popularMovieInfo = PopularMovieInfo.builder()
                .targetDate(targetDate)
                .movies(movieDetails)
                .build();

        redisRepository.save(popularMovieInfo);
        return movieDetails;
    }

    @Override
    public void updateLoginUser() {
        User user = getLoginUser();
        addRecommendations(user);
    }

    @Transactional
    @Override
    public void initRecommendations(List<Long> movieIds, double rating) {
        User user = getLoginUser();

        // Step 1: 유저 선호도 저장
        for (Long movieId : movieIds) {
            updatePreferences(movieId, rating);
        }

        // Step 2: 랜덤 추천 데이터 저장
        List<PopularMovieInfo.MovieDetail> randomMovies = getRandomPopularMovies(10);
        saveInitialRecommendations(user, randomMovies);

        // Step 3: 유저 선호도 기반 추천 데이터로 업데이트
        addRecommendations(user);
    }

    // 랜덤 인기 영화 가져오기
    private List<PopularMovieInfo.MovieDetail> getRandomPopularMovies(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        List<Movie> randomMovies = movieRepository.findTopRatedMovies(pageable).getContent();

        return randomMovies.stream()
                .map(movie -> PopularMovieInfo.MovieDetail.builder()
                        .movieId(movie.getMovieId())
                        .popularityScore(0)
                        .genreIds(movie.getMovieGenres().stream()
                                .map(genre -> genre.getGenre().getGenreId())
                                .toList())
                        .actorIds(movie.getMovieActors().stream()
                                .map(actor -> actor.getActor().getActorId())
                                .toList())
                        .directorIds(movie.getMovieDirectors().stream()
                                .map(director -> director.getDirector().getDirectorId())
                                .toList())
                        .build())
                .toList();
    }

    private void saveInitialRecommendations(User user, List<PopularMovieInfo.MovieDetail> randomMovies) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (PopularMovieInfo.MovieDetail movieDetail : randomMovies) {
            Movie movie = movieRepository.findById(movieDetail.getMovieId())
                    .orElseThrow(() -> new MovieIdNotFoundException(MOVIE_NOT_FOUND.getMessage()));

            recommendations.add(
                    Recommendation.builder()
                            .user(user)
                            .movie(movie)
                            .score(0.0) // 초기 점수는 0
                            .build()
            );
        }

        // 랜덤 추천 데이터 저장
        recommendationRepository.deleteByUser_UserId(user.getUserId());
        recommendationRepository.saveAll(recommendations);
    }


    @Transactional
    @Override
    public void addRecommendations(User user) {
        List<PopularMovieInfo.MovieDetail> popularMovies = getDailyPopularMovies();
        List<Recommendation> recommendations = new ArrayList<>();

        for (PopularMovieInfo.MovieDetail movieDetail : popularMovies) {
            Movie movie = movieRepository.findById(movieDetail.getMovieId())
                    .orElseThrow(() -> new MovieIdNotFoundException(MOVIE_NOT_FOUND.getMessage()));
            Double score = calculateRecommendationScore(user, movieDetail);
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

        recommendations.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));
        recommendationRepository.deleteByUser_UserId(user.getUserId());
        recommendationRepository.saveAll(recommendations.stream().limit(10).toList());
    }

    @Transactional
    @Override
    public void updatePreferences(Long movieId, double rating) {
        User user = getLoginUser();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieIdNotFoundException(MOVIE_NOT_FOUND.getMessage()));

        double weight = rating / 5.0;

        updateActorPreferences(user, movie, weight);
        updateDirectorPreferences(user, movie, weight);
        updateGenrePreferences(user, movie, weight);
    }

    private Double calculateRecommendationScore(User user, PopularMovieInfo.MovieDetail movieDetail) {
        Double score = 0.0;

        List<UserActorPreference> actorPreferences = userActorPreferenceRepository.findByUserAndActorIds(user, movieDetail.getActorIds());
        score += actorPreferences.stream().mapToDouble(UserActorPreference::getPreferenceScore).sum();

        List<UserDirectorPreference> directorPreferences = userDirectorPreferenceRepository.findByUserAndDirectorIds(user, movieDetail.getDirectorIds());
        score += directorPreferences.stream().mapToDouble(UserDirectorPreference::getPreferenceScore).sum();

        List<UserGenrePreference> genrePreferences = userGenrePreferenceRepository.findByUserAndGenreIds(user, movieDetail.getGenreIds());
        score += genrePreferences.stream().mapToDouble(UserGenrePreference::getPreferenceScore).sum();

        score += movieDetail.getPopularityScore();

        return score;
    }


    private void updateActorPreferences(User user, Movie movie, double weight) {
        for (MovieActors movieActor : movie.getMovieActors()) {
            Actor actor = movieActor.getActor();
            UserActorPreference preference = userActorPreferenceRepository.findByUserAndActor(user, actor);
            double scoreToAdd = weight * 5.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserActorPreference.builder()
                        .user(user)
                        .actor(actor)  // Actor 객체를 설정
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userActorPreferenceRepository.save(preference);
        }
    }


    private void updateDirectorPreferences(User user, Movie movie, double weight) {
        for (MovieDirectors movieDirector : movie.getMovieDirectors()) {
            Director director = movieDirector.getDirector();
            UserDirectorPreference preference = userDirectorPreferenceRepository.findByUserAndDirector(user, director);
            double scoreToAdd = weight * 3.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserDirectorPreference.builder()
                        .user(user)
                        .director(director)  // Director 객체를 설정
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userDirectorPreferenceRepository.save(preference);
        }
    }


    private void updateGenrePreferences(User user, Movie movie, double weight) {
        for (MovieGenres movieGenre : movie.getMovieGenres()) {
            Genre genre = movieGenre.getGenre();
            UserGenrePreference preference = userGenrePreferenceRepository.findByUserAndGenre(user, genre);
            double scoreToAdd = weight * 2.0;

            if (preference != null) {
                preference.updateScore(scoreToAdd);
            } else {
                preference = UserGenrePreference.builder()
                        .user(user)
                        .genre(genre)
                        .preferenceScore(scoreToAdd)
                        .build();
            }
            userGenrePreferenceRepository.save(preference);
        }
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
                ).toList();
    }
}
