package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Director;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserDirectorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDirectorPreferenceRepository extends JpaRepository<UserDirectorPreference, Long> {

    UserDirectorPreference findByUserAndDirector(User user, Director director);
}
