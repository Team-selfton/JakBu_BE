# JakBu Habit App Server API 명세서

## 기본 정보

- **Base URL**: `https://jakbu-api.dsmhs.kr`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json`

## 인증

대부분의 API는 JWT 토큰 인증이 필요합니다. 인증이 필요한 API 요청 시 헤더에 다음을 포함해야 합니다:

```
Authorization: Bearer {JWT_TOKEN}
```

---

## 1. 인증 API (AuthController)

### 1.1 회원가입

Account ID와 비밀번호로 회원가입합니다.

**Endpoint**: `POST /auth/signup`

**인증**: 불필요

**Request Body**:
```json
{
  "accountId": "user123",
  "password": "password123",
  "name": "홍길동"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountId | String | ✅ | 계정 ID (3-50자) |
| password | String | ✅ | 비밀번호 (최소 6자) |
| name | String | ✅ | 사용자 이름 (1-100자) |

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "name": "홍길동"
}
```

**Response 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| token | String | JWT 인증 토큰 |
| userId | Long | 사용자 ID |
| name | String | 사용자 이름 |

**에러 응답**:
- `400 Bad Request`: Account ID가 이미 존재하는 경우
  ```json
  {
    "message": "Account ID already exists"
  }
  ```
- `400 Bad Request`: 유효성 검증 실패
  ```json
  {
    "accountId": "Account ID must be between 3 and 50 characters",
    "password": "Password must be at least 6 characters"
  }
  ```

---

### 1.2 로그인

Account ID와 비밀번호로 로그인합니다.

**Endpoint**: `POST /auth/login`

**인증**: 불필요

**Request Body**:
```json
{
  "accountId": "user123",
  "password": "password123"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountId | String | ✅ | 계정 ID |
| password | String | ✅ | 비밀번호 |

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "name": "홍길동"
}
```

**에러 응답**:
- `400 Bad Request`: Account ID 또는 비밀번호가 잘못된 경우
  ```json
  {
    "message": "Invalid account ID or password"
  }
  ```

---

## 2. ToDo API (TodoController)

모든 ToDo API는 JWT 인증이 필요합니다.

### 2.1 Todo 생성

새로운 Todo를 생성합니다.

**Endpoint**: `POST /todo`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "title": "운동하기",
  "date": "2025-01-15"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | Todo 제목 |
| date | String | ✅ | 날짜 (ISO 8601 형식: YYYY-MM-DD) |

**Response** (200 OK):
```json
{
  "id": 1,
  "title": "운동하기",
  "date": "2025-01-15",
  "status": "TODO"
}
```

**Response 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | Todo ID |
| title | String | Todo 제목 |
| date | String | 날짜 (YYYY-MM-DD) |
| status | String | 상태 ("TODO" 또는 "DONE") |

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: 유효성 검증 실패

---

### 2.2 오늘의 Todo 조회

오늘 날짜의 모든 Todo를 조회합니다.

**Endpoint**: `GET /todo/today`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "title": "운동하기",
    "date": "2025-01-15",
    "status": "TODO"
  },
  {
    "id": 2,
    "title": "책 읽기",
    "date": "2025-01-15",
    "status": "DONE"
  }
]
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우

---

### 2.3 특정 날짜 Todo 조회

특정 날짜의 모든 Todo를 조회합니다.

**Endpoint**: `GET /todo/date?date=2025-01-15`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| date | String | ✅ | 날짜 (ISO 8601 형식: YYYY-MM-DD) |

**예시**:
```
GET /todo/date?date=2025-01-15
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "title": "운동하기",
    "date": "2025-01-15",
    "status": "TODO"
  },
  {
    "id": 2,
    "title": "책 읽기",
    "date": "2025-01-15",
    "status": "DONE"
  }
]
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
  ```json
  {
    "message": "Unauthorized"
  }
  ```
- `400 Bad Request`: 날짜 파라미터가 없거나 형식이 잘못된 경우
  ```json
  {
    "message": "Date parameter is required",
    "error": "INVALID_DATE",
    "status": 400
  }
  ```
  또는
  ```json
  {
    "message": "Invalid date format. Expected format: YYYY-MM-DD",
    "error": "INVALID_DATE_FORMAT",
    "status": 400
  }
  ```
- `500 Internal Server Error`: 서버 내부 오류
  ```json
  {
    "message": "서버에서 데이터를 가져오는 중 오류가 발생했습니다.",
    "error": "UNKNOWN_ERROR",
    "status": 500
  }
  ```

---

### 2.4 Todo 상태 토글 (DONE ↔ TODO)

Todo의 완료 상태를 토글합니다. TODO 상태면 DONE으로, DONE 상태면 TODO로 변경됩니다.

**Endpoint**: `POST /todo/{id}/done`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | Todo ID |

**예시**:
```
POST /todo/1/done
```

**동작 방식**:
- 현재 상태가 `TODO`인 경우 → `DONE`으로 변경
- 현재 상태가 `DONE`인 경우 → `TODO`로 변경

**Response** (200 OK):
```json
{
  "id": 1,
  "title": "운동하기",
  "date": "2025-01-15",
  "status": "DONE"
}
```

또는 (이미 DONE 상태였던 경우):
```json
{
  "id": 1,
  "title": "운동하기",
  "date": "2025-01-15",
  "status": "TODO"
}
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: Todo를 찾을 수 없거나 권한이 없는 경우
  ```json
  {
    "message": "Todo not found"
  }
  ```
  또는
  ```json
  {
    "message": "Unauthorized"
  }
  ```

### 2.5 Todo 상태 설정 (DONE/TODO 명시적 설정)

특정 Todo의 상태를 명시적으로 DONE 또는 TODO로 설정합니다.

**Endpoint**: `POST /todo/{id}/status`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | Todo ID |

**Request Body**:
```json
{
  "done": true
}
```

**필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| done | Boolean | ✅ | true면 DONE, false면 TODO |

**Response** (200 OK):
```json
{
  "id": 1,
  "title": "운동하기",
  "date": "2025-01-15",
  "status": "DONE"
}
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: Todo를 찾을 수 없거나 권한이 없는 경우
  ```json
  {
    "message": "Todo not found"
  }
  ```
  또는
  ```json
  {
    "message": "Unauthorized"
  }
  ```

---

## 3. 알림 API (NotificationController)

모든 알림 API는 JWT 인증이 필요합니다.

### 3.1 FCM 토큰 저장

사용자의 FCM 토큰을 저장합니다.

**Endpoint**: `POST /notification/token`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "fcmToken": "fcm_token_string_here"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| fcmToken | String | ✅ | Firebase Cloud Messaging 토큰 |

**Response** (200 OK):
```
(응답 본문 없음)
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: 유효성 검증 실패

---

### 3.2 알림 설정 저장

사용자의 알림 설정을 저장합니다.

**Endpoint**: `POST /notification/setting`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "intervalType": "TWO_HOUR",
  "enabled": true
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| intervalType | String | ✅ | 알림 간격 타입 ("TWO_HOUR", "FOUR_HOUR", "DAILY") |
| enabled | Boolean | ✅ | 알림 활성화 여부 |

**IntervalType 값**:
- `TWO_HOUR`: 2시간 간격
- `FOUR_HOUR`: 4시간 간격
- `DAILY`: 매일 (오전 9시)

**Response** (200 OK):
```json
{
  "id": 1,
  "intervalType": "TWO_HOUR",
  "enabled": true
}
```

**Response 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 알림 설정 ID |
| intervalType | String | 알림 간격 타입 |
| enabled | Boolean | 알림 활성화 여부 |

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: 유효성 검증 실패

---

### 3.3 알림 설정 조회

사용자의 알림 설정을 조회합니다.

**Endpoint**: `GET /notification/setting`

**인증**: 필요 (JWT)

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "intervalType": "TWO_HOUR",
  "enabled": true
}
```

**에러 응답**:
- `401 Unauthorized`: JWT 토큰이 없거나 유효하지 않은 경우
- `400 Bad Request`: 알림 설정을 찾을 수 없는 경우
  ```json
  {
    "message": "Notification setting not found"
  }
  ```

---

## 공통 에러 응답

### 401 Unauthorized
JWT 토큰이 없거나 유효하지 않은 경우:
```json
{
  "message": "Unauthorized"
}
```

### 400 Bad Request
요청 데이터 유효성 검증 실패:
```json
{
  "fieldName": "Error message"
}
```

### 500 Internal Server Error
서버 내부 오류:
```json
{
  "message": "Internal server error"
}
```

---

## 데이터 타입

### TodoStatus
- `TODO`: 미완료
- `DONE`: 완료

### IntervalType
- `TWO_HOUR`: 2시간 간격 알림
- `FOUR_HOUR`: 4시간 간격 알림
- `DAILY`: 매일 알림 (오전 9시)

---

## 예제 요청

### cURL 예제

#### 회원가입
```bash
curl -X POST https://jakbu-api.dsmhs.kr/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "user123",
    "password": "password123",
    "name": "홍길동"
  }'
```

#### 로그인
```bash
curl -X POST https://jakbu-api.dsmhs.kr/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "user123",
    "password": "password123"
  }'
```

#### Todo 생성
```bash
curl -X POST https://jakbu-api.dsmhs.kr/todo \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "title": "운동하기",
    "date": "2025-01-15"
  }'
```

#### 오늘의 Todo 조회
```bash
curl -X GET https://jakbu-api.dsmhs.kr/todo/today \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

#### 특정 날짜 Todo 조회
```bash
curl -X GET "https://jakbu-api.dsmhs.kr/todo/date?date=2025-12-09" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"
```

**주의사항**:
- 날짜 파라미터는 반드시 `YYYY-MM-DD` 형식이어야 합니다.
- 날짜 파라미터가 없거나 잘못된 형식인 경우 400 Bad Request 오류가 반환됩니다.

#### Todo 완료 상태 토글
```bash
curl -X POST https://jakbu-api.dsmhs.kr/todo/1/done \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

#### Todo 상태 설정 (명시적)
```bash
curl -X POST https://jakbu-api.dsmhs.kr/todo/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "done": true
  }'
```

#### FCM 토큰 저장
```bash
curl -X POST https://jakbu-api.dsmhs.kr/notification/token \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "fcmToken": "fcm_token_string_here"
  }'
```

#### 알림 설정 저장
```bash
curl -X POST https://jakbu-api.dsmhs.kr/notification/setting \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "intervalType": "TWO_HOUR",
    "enabled": true
  }'
```

---

## 버전 정보

- **API Version**: 1.0
- **Last Updated**: 2025-01-15

