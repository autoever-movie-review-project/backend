# 베이스 이미지로 Amazon Corretto 17 사용
FROM amazoncorretto:17

# 컨테이너 내부에서 사용할 포트를 8080으로 공개
EXPOSE 8080

# 환경변수로 전달된 JAR 파일을 복사하여 컨테이너 내부의 app.jar로 설정
# 이때, JAR 파일은 build/libs 디렉토리에 위치
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 컨테이너가 시작될 때 실행할 명령어를 지정
# Java로 app.jar 파일을 실행
ENTRYPOINT ["java","-jar","app.jar"]