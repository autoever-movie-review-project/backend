package com.movie.domain.user.exception;

import com.movie.global.exception.NotFoundException;

public class UserIdNotFoundException extends NotFoundException {
  public UserIdNotFoundException(String message) {
    super(message);
  }
}
