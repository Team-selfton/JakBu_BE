# 환경변수 설정 가이드

## 필수 환경변수

### 데이터베이스 설정
```bash
export DB_URL="jdbc:mysql://your-mysql-host:3306/jakbu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
export DB_USERNAME="your_db_username"
export DB_PASSWORD="your_db_password"
```

### JWT 설정
```bash
export JWT_SECRET="your-jwt-secret-key-minimum-256-bits-for-hs256-algorithm"
export JWT_EXPIRATION=86400000  # 24 hours in milliseconds (선택사항, 기본값: 86400000)
```

### Firebase 설정
```bash
# Firebase 서비스 계정 JSON 파일을 base64로 인코딩
export FIREBASE_CONFIG_BASE64="$(cat firebase-admin.json | base64)"
```

또는 직접 base64 인코딩된 문자열:
```bash
export FIREBASE_CONFIG_BASE64="eyJ0eXBlIjoic2VydmljZV9hY2NvdW50Iiwi..."
```

### 서버 포트 (선택사항)
```bash
export SERVER_PORT=8080  # 기본값: 8080
```

## Kubernetes Secret 예시

### Secret 생성
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: jakbu-secrets
type: Opaque
stringData:
  DB_URL: "jdbc:mysql://mysql-service:3306/jakbu?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
  DB_USERNAME: "jakbu_user"
  DB_PASSWORD: "your_secure_password"
  JWT_SECRET: "your-jwt-secret-key-minimum-256-bits"
  JWT_EXPIRATION: "86400000"
  FIREBASE_CONFIG_BASE64: "eyJ0eXBlIjoic2VydmljZV9hY2NvdW50Iiwi..."
  SERVER_PORT: "8080"
```

### Deployment에서 사용
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jakbu-backend
spec:
  template:
    spec:
      containers:
      - name: jakbu-backend
        image: jakbu-backend:latest
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: DB_URL
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: DB_USERNAME
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: JWT_SECRET
        - name: JWT_EXPIRATION
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: JWT_EXPIRATION
        - name: FIREBASE_CONFIG_BASE64
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: FIREBASE_CONFIG_BASE64
        - name: SERVER_PORT
          valueFrom:
            secretKeyRef:
              name: jakbu-secrets
              key: SERVER_PORT
```

## Firebase Config Base64 인코딩 방법

### Linux/Mac
```bash
base64 -i firebase-admin.json | tr -d '\n'
```

### 또는
```bash
cat firebase-admin.json | base64 | tr -d '\n'
```

### Windows (PowerShell)
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("firebase-admin.json"))
```

## 환경변수 확인

애플리케이션 시작 시 환경변수가 제대로 설정되었는지 확인:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`가 설정되지 않으면 데이터베이스 연결 실패
- `JWT_SECRET`이 설정되지 않으면 애플리케이션 시작 실패
- `FIREBASE_CONFIG_BASE64`가 설정되지 않으면 Firebase 초기화 실패

## 보안 주의사항

1. **절대 환경변수를 코드에 하드코딩하지 마세요**
2. **프로덕션 환경에서는 반드시 Kubernetes Secret 또는 다른 시크릿 관리 시스템 사용**
3. **JWT_SECRET은 최소 256비트(32바이트) 이상의 랜덤 문자열 사용 권장**
4. **Firebase 서비스 계정 키는 절대 Git에 커밋하지 마세요**

