package com.movie.domain.user.dao;

import com.movie.domain.user.domain.UserActorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserActorPreference, Long> {

}
