package com.movie.domain.user.dao;

import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.domain.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByEmail(String email);
}
