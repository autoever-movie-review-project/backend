package com.movie.domain.user.dao;

import com.movie.domain.movie.domain.Actor;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserActorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserActorPreferenceRepository extends JpaRepository<UserActorPreference, Long> {

    UserActorPreference findByUserAndActor(User user, Actor actor);

    @Query("SELECT uap FROM UserActorPreference uap WHERE uap.user = :user AND uap.actor = :actor AND uap.modifiedAt >= :recentDate")
    UserActorPreference findRecentByUserAndActor(@Param("user") User user, @Param("actor") Actor actor, @Param("recentDate") LocalDate recentDate);
}
