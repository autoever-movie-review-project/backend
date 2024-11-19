package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Genre;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserGenrePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

    UserGenrePreference findByUserAndGenre(User user, Genre genre);

    @Query("SELECT ugp FROM UserGenrePreference ugp WHERE ugp.user = :user AND ugp.genre = :genre AND ugp.modifiedAt >= :recentDate")
    UserGenrePreference findRecentByUserAndGenre(@Param("user") User user, @Param("genre") Genre genre, @Param("recentDate") LocalDate recentDate);

    List<UserGenrePreference> findByUserAndGenreIn(User user, List<Genre> genres);

    @Query("SELECT ugp FROM UserGenrePreference ugp " +
            "JOIN ugp.genre genre " +
            "WHERE ugp.user = :user AND genre.genreId IN :genreIds")
    List<UserGenrePreference> findByUserAndGenreIds(@Param("user") User user, @Param("genreIds") List<Integer> genreIds);

}
