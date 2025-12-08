# 카카오 OAuth 로그인 구현 가이드

## 개요

서버가 카카오 OAuth 로그인 전체 플로우를 자동으로 처리하는 방식으로 구현되었습니다.

## 구현된 기능

### 1. 카카오 로그인 시작
- **엔드포인트**: `GET /auth/kakao/login`
- **기능**: 카카오 로그인 페이지로 자동 리다이렉트

### 2. 카카오 OAuth 콜백 처리
- **엔드포인트**: `GET /auth/kakao/callback?code=xxx`
- **기능**: 
  - 인가 코드로 액세스 토큰 발급
  - 액세스 토큰으로 사용자 정보 조회
  - DB에서 사용자 조회/생성
  - JWT 발급 및 응답

## 설정 방법

### 1. application.yml 설정

```yaml
kakao:
  oauth:
    client-id: YOUR_KAKAO_REST_API_KEY  # 카카오 개발자 콘솔에서 발급받은 REST API 키
    client-secret: YOUR_KAKAO_CLIENT_SECRET  # 카카오 개발자 콘솔에서 발급받은 Client Secret
    redirect-uri: http://localhost:8080/auth/kakao/callback  # 카카오 개발자 콘솔에 등록한 Redirect URI
    authorization-uri: https://kauth.kakao.com/oauth/authorize
    token-uri: https://kauth.kakao.com/oauth/token
    user-info-uri: https://kapi.kakao.com/v2/user/me
    frontend-redirect-url: http://localhost:3000/login  # 리다이렉트 모드 사용 시 프론트엔드 URL
```

### 2. 카카오 개발자 콘솔 설정

1. [카카오 개발자 콘솔](https://developers.kakao.com/) 접속
2. 내 애플리케이션 > 앱 설정 > 플랫폼
   - Web 플랫폼 등록: `http://localhost:8080`
3. 내 애플리케이션 > 앱 설정 > 앱 키
   - REST API 키 복사 → `client-id`에 설정
4. 내 애플리케이션 > 제품 설정 > 카카오 로그인
   - Redirect URI 등록: `http://localhost:8080/auth/kakao/callback`
   - Client Secret 발급 → `client-secret`에 설정

## 사용 방법

### 방법 1: JSON 응답 (기본)

클라이언트가 `/auth/kakao/login`을 호출하면 카카오 로그인 페이지로 리다이렉트되고, 로그인 완료 후 `/auth/kakao/callback`에서 JSON으로 응답합니다.

**요청 플로우**:
```
1. GET /auth/kakao/login
   → 카카오 로그인 페이지로 리다이렉트

2. 사용자가 카카오 로그인 완료
   → GET /auth/kakao/callback?code=xxx 자동 호출

3. 서버 응답 (JSON):
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "userId": 1,
     "name": "홍길동"
   }
```

**예시 (Flutter)**:
```dart
// 1. 카카오 로그인 시작
launchUrl(Uri.parse('http://localhost:8080/auth/kakao/login'));

// 2. WebView에서 callback URL을 감지하여 토큰 추출
// 또는 서버에서 리다이렉트 모드 사용 (방법 2 참조)
```

### 방법 2: 리다이렉트 기반 응답

프론트엔드로 JWT 토큰을 포함한 URL로 리다이렉트합니다.

**요청 플로우**:
```
1. GET /auth/kakao/login
   → 카카오 로그인 페이지로 리다이렉트

2. 사용자가 카카오 로그인 완료
   → GET /auth/kakao/callback?code=xxx&redirect=true&redirectUrl=http://localhost:3000/login

3. 서버 응답 (리다이렉트):
   → http://localhost:3000/login?token=JWT_TOKEN&userId=1&name=홍길동
```

**예시 (Flutter)**:
```dart
// WebView에서 리다이렉트 URL 감지
webViewController.setNavigationDelegate(
  NavigationDelegate(
    onNavigationRequest: (NavigationRequest request) {
      if (request.url.startsWith('http://localhost:3000/login')) {
        // URL에서 토큰 추출
        final uri = Uri.parse(request.url);
        final token = uri.queryParameters['token'];
        final userId = uri.queryParameters['userId'];
        final name = uri.queryParameters['name'];
        
        // 토큰 저장 및 로그인 처리
        // ...
        
        return NavigationDecision.prevent;
      }
      return NavigationDecision.navigate;
    },
  ),
);
```

## API 엔드포인트 상세

### GET /auth/kakao/login

카카오 로그인 페이지로 리다이렉트합니다.

**요청**:
```
GET /auth/kakao/login
```

**응답**: 
- HTTP 302 Redirect
- Location: `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=...&redirect_uri=...`

---

### GET /auth/kakao/callback

카카오 OAuth 콜백을 처리합니다.

**요청**:
```
GET /auth/kakao/callback?code=xxx
```

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| code | String | ✅ | 카카오에서 전달받은 인가 코드 |
| error | String | ❌ | 에러 코드 (카카오 로그인 취소 시) |
| redirect | Boolean | ❌ | 리다이렉트 모드 사용 여부 (기본값: false) |
| redirectUrl | String | ❌ | 리다이렉트 URL (redirect=true일 때 사용) |

**응답 (JSON 모드, redirect=false 또는 미지정)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "name": "홍길동"
}
```

**응답 (리다이렉트 모드, redirect=true)**:
- HTTP 302 Redirect
- Location: `{redirectUrl}?token=JWT_TOKEN&userId=1&name=홍길동`

**에러 응답**:
```json
{
  "message": "Failed to process Kakao OAuth: ..."
}
```

---

## 구현된 클래스

### 1. KakaoOAuthService
- `getAccessToken(String code)`: 인가 코드로 액세스 토큰 발급
- `getUserInfo(String accessToken)`: 액세스 토큰으로 사용자 정보 조회
- `processKakaoLogin(KakaoUserInfoResponse)`: 사용자 로그인/회원가입 처리 및 JWT 발급

### 2. AuthController
- `GET /auth/kakao/login`: 카카오 로그인 시작
- `GET /auth/kakao/callback`: 카카오 OAuth 콜백 처리

### 3. DTO
- `KakaoTokenResponse`: 카카오 토큰 응답
- `KakaoUserInfoResponse`: 카카오 사용자 정보 응답

## 보안 고려사항

1. **Client Secret 보안**: 
   - 프로덕션 환경에서는 환경 변수나 시크릿 관리 시스템 사용 권장
   - `application.yml`에 직접 작성하지 않도록 주의

2. **Redirect URI 검증**:
   - 카카오 개발자 콘솔에 등록한 Redirect URI와 정확히 일치해야 함
   - 프로덕션 환경에서는 HTTPS 사용 필수

3. **에러 처리**:
   - 카카오 API 호출 실패 시 적절한 에러 메시지 반환
   - 사용자에게 친화적인 에러 메시지 제공

## 테스트 방법

### 1. 로컬 테스트

```bash
# 1. 서버 실행
./gradlew bootRun

# 2. 브라우저에서 접속
http://localhost:8080/auth/kakao/login

# 3. 카카오 로그인 완료 후 callback URL 확인
# JSON 응답 또는 리다이렉트 URL 확인
```

### 2. cURL 테스트

```bash
# 카카오 로그인 시작 (리다이렉트 URL 확인)
curl -v http://localhost:8080/auth/kakao/login

# 콜백 테스트 (실제 code는 카카오 로그인 후 받은 값 사용)
curl "http://localhost:8080/auth/kakao/callback?code=ACTUAL_CODE_FROM_KAKAO"
```

## 문제 해결

### 1. "Invalid redirect URI" 에러
- 카카오 개발자 콘솔에 등록한 Redirect URI와 `application.yml`의 `redirect-uri`가 정확히 일치하는지 확인

### 2. "Invalid client" 에러
- `client-id`와 `client-secret`이 올바른지 확인
- 카카오 개발자 콘솔에서 Client Secret이 발급되었는지 확인

### 3. "Failed to get Kakao access token" 에러
- 인가 코드가 유효한지 확인 (인가 코드는 1회만 사용 가능)
- 카카오 API 서버 상태 확인

## 추가 기능

### 기존 방식 유지
기존의 클라이언트가 accessToken을 전달하는 방식도 계속 사용 가능합니다:
- `POST /auth/kakao` (기존 엔드포인트)

이 방식은 하위 호환성을 위해 유지되었습니다.

