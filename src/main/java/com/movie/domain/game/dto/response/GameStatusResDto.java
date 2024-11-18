package com.movie.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStatusResDto {
    private Long gameId;
    private boolean isDeleted;
    private Long newHostId;
}
