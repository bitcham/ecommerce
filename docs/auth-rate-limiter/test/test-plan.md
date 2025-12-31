# Auth Controller Rate Limiter Test Plan

## Test Scope

AuthController의 Rate Limiter가 올바르게 동작하는지 검증한다.

---

## Test Categories

### 1. Rate Limiter 적용 확인 테스트

각 엔드포인트에 Rate Limiter가 적용되어 있는지 확인:

| Endpoint | Rate Limiter Name | Expected Limit |
|----------|------------------|----------------|
| `POST /login` | auth | 5/min |
| `POST /register` | register | 3/hour |
| `POST /forgot-password` | email-action | 3/hour |
| `POST /resend-verification` | email-action | 3/hour |
| `POST /reset-password` | sensitive-action | 5/hour |
| `POST /refresh` | token-action | 10/min |
| `GET /verify-email` | token-action | 10/min |

### 2. Rate Limit 초과 테스트

제한 횟수를 초과할 때 `429 Too Many Requests` 응답 확인:

- [ ] login: 5회 초과 시 429 응답
- [ ] register: 3회 초과 시 429 응답
- [ ] forgot-password: 3회 초과 시 429 응답
- [ ] resend-verification: 3회 초과 시 429 응답
- [ ] reset-password: 5회 초과 시 429 응답
- [ ] refresh: 10회 초과 시 429 응답
- [ ] verify-email: 10회 초과 시 429 응답

### 3. Rate Limit 미적용 테스트

Rate Limiter가 필요 없는 엔드포인트 확인:

- [ ] logout: Rate limit 없음 확인
- [ ] logout-all: Rate limit 없음 확인

### 4. 에러 응답 포맷 테스트

429 응답 시 올바른 에러 포맷 반환 확인:

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Please try again later."
}
```

---

## Test Environment

### 테스트용 Rate Limiter 설정

테스트 속도를 위해 `application-test.yml`에서 제한값을 낮춤:

```yaml
resilience4j:
  ratelimiter:
    instances:
      auth:
        limit-for-period: 2
        limit-refresh-period: 1s
      register:
        limit-for-period: 2
        limit-refresh-period: 1s
      email-action:
        limit-for-period: 2
        limit-refresh-period: 1s
      sensitive-action:
        limit-for-period: 2
        limit-refresh-period: 1s
      token-action:
        limit-for-period: 2
        limit-refresh-period: 1s
```

---

## Test Cases

### TC-001: Login Rate Limit

**Given:** Rate Limiter 'auth' 설정 (2회/초)
**When:** 3회 연속 로그인 시도
**Then:** 3번째 요청은 429 응답

### TC-002: Register Rate Limit

**Given:** Rate Limiter 'register' 설정 (2회/초)
**When:** 3회 연속 회원가입 시도
**Then:** 3번째 요청은 429 응답

### TC-003: Forgot Password Rate Limit

**Given:** Rate Limiter 'email-action' 설정 (2회/초)
**When:** 3회 연속 비밀번호 찾기 요청
**Then:** 3번째 요청은 429 응답

### TC-004: Resend Verification Rate Limit

**Given:** Rate Limiter 'email-action' 설정 (2회/초)
**When:** 3회 연속 인증 이메일 재발송 요청
**Then:** 3번째 요청은 429 응답

### TC-005: Reset Password Rate Limit

**Given:** Rate Limiter 'sensitive-action' 설정 (2회/초)
**When:** 3회 연속 비밀번호 재설정 요청
**Then:** 3번째 요청은 429 응답

### TC-006: Refresh Token Rate Limit

**Given:** Rate Limiter 'token-action' 설정 (2회/초)
**When:** 3회 연속 토큰 갱신 요청
**Then:** 3번째 요청은 429 응답

### TC-007: Verify Email Rate Limit

**Given:** Rate Limiter 'token-action' 설정 (2회/초)
**When:** 3회 연속 이메일 인증 요청
**Then:** 3번째 요청은 429 응답

### TC-008: Logout No Rate Limit

**Given:** Rate Limiter 없음
**When:** 10회 연속 로그아웃 요청
**Then:** 모든 요청 성공 (429 없음)

### TC-009: Error Response Format

**Given:** Rate Limit 초과 상태
**When:** 추가 요청
**Then:** 응답 body에 code, message 포함

---

## Edge Cases

- [ ] Rate limit 초과 후 시간 경과로 리셋 확인
- [ ] 같은 IP에서 다른 엔드포인트는 별도 카운팅
- [ ] email-action을 공유하는 엔드포인트 간 카운트 공유 확인
