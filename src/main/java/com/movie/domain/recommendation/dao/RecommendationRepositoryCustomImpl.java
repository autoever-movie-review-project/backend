//package com.movie.domain.recommendation.dao;
//
//import com.movie.domain.movie.domain.*;
//import com.movie.domain.user.domain.QUserActorPreference;
//import com.movie.domain.user.domain.QUserDirectorPreference;
//import com.movie.domain.user.domain.QUserGenrePreference;
//import com.querydsl.core.types.dsl.Expressions;
//import com.querydsl.core.types.dsl.NumberPath;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//@RequiredArgsConstructor
//public class RecommendationRepositoryCustomImpl implements RecommendationRepositoryCustom {
//
//    private final JPAQueryFactory queryFactory;
//
//    @Override
//    public List<Movie> findRecommendedMoviesByUserPreferences(Long userId) {
//        QMovie movie = QMovie.movie;
//        QMovieActors movieActors = QMovieActors.movieActors;
//        QMovieDirectors movieDirectors = QMovieDirectors.movieDirectors;
//        QMovieGenres movieGenres = QMovieGenres.movieGenres;
//
//        QUserActorPreference userActorPreference = QUserActorPreference.userActorPreference;
//        QUserDirectorPreference userDirectorPreference = QUserDirectorPreference.userDirectorPreference;
//        QUserGenrePreference userGenrePreference = QUserGenrePreference.userGenrePreference;
//
//        NumberPath<Long> matchCount = Expressions.numberPath(Long.class, "matchCount");
//
//        return queryFactory.select(movie)
//                .from(movie)
//                .leftJoin(movieActors).on(movieActors.movie.movieId.eq(movie.movieId))
//                .leftJoin(userActorPreference).on(userActorPreference.actor.actorId.eq(movieActors.actor.actorId)
//                        .and(userActorPreference.user.userId.eq(userId)))
//                .leftJoin(movieDirectors).on(movieDirectors.movie.movieId.eq(movie.movieId))
//                .leftJoin(userDirectorPreference).on(userDirectorPreference.director.directorId.eq(movieDirectors.director.directorId)
//                        .and(userDirectorPreference.user.userId.eq(userId)))
//                .leftJoin(movieGenres).on(movieGenres.movie.movieId.eq(movie.movieId))
//                .leftJoin(userGenrePreference).on(userGenrePreference.genre.genreId.eq(movieGenres.genre.genreId)
//                        .and(userGenrePreference.user.userId.eq(userId)))
//                .groupBy(movie.movieId)
//                .orderBy(userActorPreference.actor.count().add(
//                                userDirectorPreference.director.count())
//                        .add(userGenrePreference.genre.count()).desc())
//                .fetch();
//    }
//}
