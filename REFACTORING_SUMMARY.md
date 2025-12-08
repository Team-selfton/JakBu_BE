# 카카오 OAuth2 로그인 리팩토링 요약

## 📋 리팩토링 완료 사항

### ✅ 변경된 파일

#### 1. **AuthController.java** (간소화)
- **제거**: `POST /auth/kakao` 엔드포인트 (기존 클라이언트 accessToken 전달 방식)
- **제거**: 리다이렉트 모드 관련 파라미터 및 로직
- **유지**: `GET /auth/kakao/login` - 카카오 로그인 시작
- **유지**: `GET /auth/kakao/callback` - OAuth 콜백 처리 (JSON 응답만)

#### 2. **KakaoOAuthService.java** (메서드명 변경 및 로직 개선)
- **변경**: `getAccessToken()` → `requestToken()`
- **변경**: `getUserInfo()` → `requestUserInfo()`
- **변경**: `processKakaoLogin()` → `processLogin()`
- **개선**: email, nickname 기반 사용자 조회 로직 추가
- **개선**: 기존 사용자에 카카오 계정 연결 기능 추가

#### 3. **AuthService.java** (카카오 관련 코드 제거)
- **제거**: `kakaoLogin()` 메서드
- **제거**: `getKakaoUserInfo()` 메서드
- **제거**: WebClient 의존성

#### 4. **User.java** (소셜 로그인 팩토리 메서드 개선)
- **변경**: `createKakaoUser(Long kakaoId, String name)` → `createKakaoUser(Long kakaoId, String name, String email)`
- **추가**: `linkKakaoAccount(User user, Long kakaoId)` - 기존 사용자에 카카오 계정 연결

#### 5. **application.yml** (정리)
- **제거**: `frontend-redirect-url` 설정
- **정리**: 주석 간소화

### ❌ 삭제된 파일

1. **KakaoLoginRequest.java** - 더 이상 사용하지 않음

### ✅ 유지된 파일

1. **KakaoTokenResponse.java** - 카카오 토큰 응답 DTO
2. **KakaoUserInfoResponse.java** - 카카오 사용자 정보 응답 DTO

---

## 🏗️ 최종 구조

```
src/main/java/com/jakbu
 ├── controller
 │     └── AuthController.java          ✅ 간소화됨
 ├── service
 │     ├── AuthService.java             ✅ 카카오 코드 제거
 │     └── KakaoOAuthService.java       ✅ 메서드명 변경 및 로직 개선
 ├── dto
 │     ├── KakaoTokenResponse.java      ✅ 유지
 │     └── KakaoUserInfoResponse.java   ✅ 유지
 ├── repository
 │     └── UserRepository.java          ✅ 변경 없음
 ├── domain
 │     └── User.java                    ✅ 팩토리 메서드 개선
 └── util
       └── JwtUtil.java                 ✅ 변경 없음
```

---

## 🔧 주요 변경 내용

### 1. KakaoOAuthService 메서드

#### `requestToken(String code)`
- 인가 코드로 카카오 액세스 토큰 발급
- WebClient 기반 POST 요청
- 반환: `KakaoTokenResponse`

#### `requestUserInfo(String accessToken)`
- 액세스 토큰으로 카카오 사용자 정보 조회
- WebClient 기반 GET 요청
- 반환: `KakaoUserInfoResponse`

#### `processLogin(KakaoUserInfoResponse kakaoUserInfo)`
- email, nickname 기반으로 회원 조회
- 없으면 새 User 생성 후 저장
- JWT 생성 후 반환
- 반환: `AuthResponse`

### 2. User 엔티티 팩토리 메서드

#### `createKakaoUser(Long kakaoId, String name, String email)`
- 카카오 소셜 로그인 사용자 생성
- email 파라미터 추가 (nullable)

#### `linkKakaoAccount(User user, Long kakaoId)`
- 기존 사용자 계정에 카카오 계정 연결
- 이메일로 기존 사용자를 찾았을 때 사용

---

## 📝 API 엔드포인트

### GET /auth/kakao/login
카카오 로그인 페이지로 리다이렉트

**요청**:
```
GET /auth/kakao/login
```

**응답**: HTTP 302 Redirect
```
Location: https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=xxx&redirect_uri=xxx
```

---

### GET /auth/kakao/callback
카카오 OAuth 콜백 처리 및 JWT 반환

**요청**:
```
GET /auth/kakao/callback?code=xxx
```

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| code | String | ✅ | 카카오 인가 코드 |
| error | String | ❌ | 에러 코드 (카카오 로그인 취소 시) |

**응답** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "name": "홍길동"
}
```

**에러 응답** (400 Bad Request):
```json
{
  "message": "Kakao OAuth error: ..."
}
```

---

## 🧪 테스트 방법

### 1. 카카오 로그인 시작
```bash
curl -v http://localhost:8080/auth/kakao/login
```

### 2. 카카오 콜백 테스트
```bash
# 실제 인가 코드는 카카오 로그인 후 받은 값 사용
curl "http://localhost:8080/auth/kakao/callback?code=ACTUAL_CODE_FROM_KAKAO"
```

### 3. 전체 플로우 테스트
1. 브라우저에서 `http://localhost:8080/auth/kakao/login` 접속
2. 카카오 로그인 완료
3. 콜백 URL에서 JWT 토큰 확인

---

## ⚙️ application.yml 설정

```yaml
kakao:
  oauth:
    client-id: YOUR_KAKAO_REST_API_KEY
    client-secret: YOUR_KAKAO_CLIENT_SECRET
    redirect-uri: http://localhost:8080/auth/kakao/callback
    authorization-uri: https://kauth.kakao.com/oauth/authorize
    token-uri: https://kauth.kakao.com/oauth/token
    user-info-uri: https://kapi.kakao.com/v2/user/me
```

---

## 🔄 로그인 플로우

```
1. 클라이언트 → GET /auth/kakao/login
   ↓
2. 서버 → 카카오 로그인 페이지로 리다이렉트
   ↓
3. 사용자 → 카카오 로그인 완료
   ↓
4. 카카오 → GET /auth/kakao/callback?code=xxx
   ↓
5. 서버 → KakaoOAuthService.requestToken(code)
   ↓
6. 서버 → KakaoOAuthService.requestUserInfo(accessToken)
   ↓
7. 서버 → KakaoOAuthService.processLogin(userInfo)
   ↓
8. 서버 → JWT 발급 및 반환
```

---

## ✨ 개선 사항

1. **코드 간소화**: 불필요한 리다이렉트 모드 제거
2. **책임 분리**: AuthService에서 카카오 관련 코드 제거
3. **메서드명 명확화**: `requestToken`, `requestUserInfo`, `processLogin`으로 변경
4. **사용자 조회 로직 개선**: email, nickname 기반 조회 추가
5. **계정 연결 기능**: 기존 사용자에 카카오 계정 연결 가능

---

## 📌 주의사항

1. **카카오 개발자 콘솔 설정**
   - Redirect URI: `http://localhost:8080/auth/kakao/callback`
   - REST API 키와 Client Secret 설정 필요

2. **프로덕션 환경**
   - `client-secret`은 환경 변수나 시크릿 관리 시스템 사용 권장
   - Redirect URI는 HTTPS 사용 필수

3. **에러 처리**
   - 카카오 API 호출 실패 시 적절한 에러 메시지 반환
   - GlobalExceptionHandler에서 처리

---

## ✅ 리팩토링 완료 체크리스트

- [x] AuthController 간소화 (두 엔드포인트만 유지)
- [x] KakaoOAuthService 메서드명 변경
- [x] AuthService에서 카카오 관련 코드 제거
- [x] User 엔티티 팩토리 메서드 개선
- [x] application.yml 정리
- [x] 불필요한 DTO 제거
- [x] 린터 오류 없음
- [x] 컴파일 성공

---

**리팩토링 완료일**: 2025-01-15

