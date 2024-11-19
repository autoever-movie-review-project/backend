package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Director;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserDirectorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserDirectorPreferenceRepository extends JpaRepository<UserDirectorPreference, Long> {

    UserDirectorPreference findByUserAndDirector(User user, Director director);

    @Query("SELECT udp FROM UserDirectorPreference udp WHERE udp.user = :user AND udp.director = :director AND udp.modifiedAt >= :recentDate")
    UserDirectorPreference findRecentByUserAndDirector(@Param("user") User user, @Param("director") Director director, @Param("recentDate") LocalDate recentDate);

    List<UserDirectorPreference> findByUserAndDirectorIn(User user, List<Director> directors);

    @Query("SELECT udp FROM UserDirectorPreference udp " +
            "JOIN udp.director director " +
            "WHERE udp.user = :user AND director.directorId IN :directorIds")
    List<UserDirectorPreference> findByUserAndDirectorIds(@Param("user") User user, @Param("directorIds") List<Long> directorIds);

}
