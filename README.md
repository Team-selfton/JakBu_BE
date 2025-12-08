# JakBu Habit App Server

Spring Boot 3.x + Java 17 + Gradle 기반의 습관 관리 앱 백엔드 서버입니다.

## 기술 스택

- **Spring Boot 3.2.0**
- **Java 17**
- **Gradle**
- **MySQL**
- **JWT (io.jsonwebtoken)**
- **Firebase Admin SDK**
- **Spring Security**

## 주요 기능

### 1. 인증 기능
- 일반 회원가입/로그인 (이메일 + 비밀번호)
- 카카오 소셜 로그인
- JWT 기반 인증

### 2. ToDo 기능
- Todo 생성
- 오늘의 Todo 조회
- 특정 날짜 Todo 조회
- Todo 완료 처리

### 3. 알림 기능
- FCM 푸시 알림
- 알림 설정 (2시간/4시간/일일 간격)
- 스케줄러 기반 반복 알림

## 프로젝트 구조

```
com.jakbu
 ├─ config          # 설정 클래스 (Security, Firebase, JPA)
 ├─ controller      # REST API 컨트롤러
 ├─ service         # 비즈니스 로직
 ├─ repository      # 데이터 접근 계층
 ├─ domain          # 엔티티 및 Enum
 │    ├─ enums
 ├─ dto             # 데이터 전송 객체
 ├─ util            # 유틸리티 (JWT)
 └─ exception       # 예외 처리
```

## 설정

### 1. application.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jakbu
    username: root
    password: your_password

jwt:
  secret: your-secret-key-minimum-256-bits
  expiration: 86400000

firebase:
  config-path: firebase-admin.json
```

### 2. Firebase 설정

1. Firebase Console에서 서비스 계정 키를 다운로드
2. `firebase-admin.json` 파일을 프로젝트 루트에 배치
3. `application.yml`의 `firebase.config-path`에 경로 설정

### 3. 데이터베이스 설정

MySQL 데이터베이스 `jakbu`를 생성하세요:

```sql
CREATE DATABASE jakbu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## API 엔드포인트

### 인증 (AuthController)
- `POST /auth/signup` - 회원가입
- `POST /auth/login` - 로그인
- `POST /auth/kakao` - 카카오 로그인

### ToDo (TodoController)
- `POST /todo` - Todo 생성
- `GET /todo/today` - 오늘의 Todo 조회
- `GET /todo/date?date=2025-01-01` - 특정 날짜 Todo 조회
- `POST /todo/{id}/done` - Todo 완료 처리

### 알림 (NotificationController)
- `POST /notification/token` - FCM 토큰 저장
- `POST /notification/setting` - 알림 설정 저장
- `GET /notification/setting` - 알림 설정 조회

## 실행 방법

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/jakbu-0.0.1-SNAPSHOT.jar
```

## 요구사항

- Java 17 이상
- MySQL 8.0 이상
- Firebase 프로젝트 및 서비스 계정 키
