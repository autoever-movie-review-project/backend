package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.BoxOfficeMovieInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxOfficeRedisRepository extends CrudRepository<BoxOfficeMovieInfo, String> {
}
