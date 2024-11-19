package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Actor;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserActorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActorPreferenceRepository extends JpaRepository<UserActorPreference, Long> {

    UserActorPreference findByUserAndActor(User user, Actor actor);
}
