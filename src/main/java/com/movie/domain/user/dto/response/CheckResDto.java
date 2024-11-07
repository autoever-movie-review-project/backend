package com.movie.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckResDto {
    private final Boolean success;
}
