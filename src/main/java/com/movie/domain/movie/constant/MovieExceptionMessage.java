package com.movie.domain.movie.constant;

public enum MovieExceptionMessage {

    MOVIEID_NOT_FOUND("존재하지 않는 영화 ID 입니다"),
    MOVIE_NOT_FOUND("존재하지 않는 영화입니다."),
    MOVIE_SAVE_ERROR("영화 정보를 저장할 수 없습니다."),
    TMDB_API_CALL_FAILED("TMDB API 호출 중 오류가 발생했습니다. TMDB ID: %d"),
    INVALID_MOVIE_DATA("영화 데이터가 유효하지 않습니다."),
    BIRTHDATE_FETCH_FAILED("생일 정보를 가져오는 데 실패했습니다. TMDB Person ID: %d"),
    AGE_RATING_FETCH_FAILED("관람 등급 정보를 가져오는 데 실패했습니다. TMDB ID: %d"),
    RELEASE_DATE_PARSE_FAILED("날짜를 파싱하는 데 실패했습니다. 입력 값: %s"),
    BOX_OFFICE_DATA_FETCH_FAILED("박스오피스 데이터를 가져올 수 없습니다.");

    private final String message;

    MovieExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}