package com.movie.domain.user.dao;

import com.movie.domain.user.domain.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserInfoRedisRepository extends CrudRepository<UserInfo, String> {
    Optional<UserInfo> findByEmail(String email);
}
