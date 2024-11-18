package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.TopReviewMovieInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopReviewRedisRepository extends CrudRepository<TopReviewMovieInfo, String> {
}
