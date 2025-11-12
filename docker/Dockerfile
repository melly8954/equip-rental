# 1. JDK 베이스 이미지 선택 (Java 17)
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. jar 파일 복사
COPY ../build/libs/equip-rental-0.0.1-SNAPSHOT.jar app.jar

# 4. 포트 노출 (예: 8080)
EXPOSE 8080

# 5. 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]