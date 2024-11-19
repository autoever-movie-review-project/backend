package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.PopularMovieInfo;
import org.springframework.data.repository.CrudRepository;

public interface PopularMovieRedisRepository extends CrudRepository<PopularMovieInfo, String> {
}
