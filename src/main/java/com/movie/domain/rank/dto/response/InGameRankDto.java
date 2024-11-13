package com.movie.domain.rank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InGameRankDto {
    private Integer rankId;
    private String rankName;
    private String rankImg;
}
