package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Genre;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserGenrePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

    UserGenrePreference findByUserAndGenre(User user, Genre genre);
}
