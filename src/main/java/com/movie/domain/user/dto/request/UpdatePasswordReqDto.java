package com.movie.domain.user.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class UpdatePasswordReqDto {
    private String password;

    public UpdatePasswordReqDto(String password) {
        this.password = password;
    }
}
