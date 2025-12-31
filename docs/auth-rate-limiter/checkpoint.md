# Auth Rate Limiter Checkpoint

**Date**: 2025-12-31
**Feature**: AuthController Rate Limiting
**Status**: ✅ Complete

---

## Summary

AuthController의 모든 민감한 엔드포인트에 Resilience4j 기반 Rate Limiting을 적용하여 brute-force 공격, 이메일 폭탄 공격, 서비스 남용을 방지. 기존 auth checkpoint의 TODO 항목 완료.

## Completed Items

### Documentation
- [x] `docs/auth-rate-limiter/detail/plan.md` - Implementation plan
- [x] `docs/auth-rate-limiter/test/test-plan.md` - Test scenarios

### Controller Layer
- [x] `AuthController.java` - 5개 엔드포인트에 `@RateLimiter` 추가

### Exception Handling
- [x] `GlobalExceptionHandler.java` - `RequestNotPermitted` 예외 처리 (429 응답)

### Configuration
- [x] `application.yml` - 3개 Rate Limiter 인스턴스 추가
- [x] `application-test.yml` - 테스트용 Rate Limiter 설정

### Test Layer
- [x] `AuthControllerRateLimiterTest.java` - 10 tests (MockMvcTester 사용)
- [x] `TestRateLimiterConfig.java` - 재사용 가능한 테스트 설정

## TDD Workflow Followed

```
1. ✅ plan.md written
2. ✅ test-plan.md written
3. ✅ Tests written (Red - 8 failed)
4. ✅ Implementation completed (Green - 10 passed)
5. ✅ Senior Review (code quality, readability, patterns)
6. ✅ Lead Review (architecture, scalability, security)
7. ✅ Refactor (no changes needed)
8. ✅ checkpoint.md written
```

## Test Coverage

| Test Category | Tests | Status |
|--------------|-------|--------|
| forgot-password Rate Limit | 1 | ✅ |
| resend-verification Rate Limit | 1 | ✅ |
| reset-password Rate Limit | 1 | ✅ |
| refresh Rate Limit | 1 | ✅ |
| verify-email Rate Limit | 1 | ✅ |
| logout No Rate Limit | 2 | ✅ |
| login Rate Limit (existing) | 1 | ✅ |
| register Rate Limit (existing) | 1 | ✅ |
| Error Response Format | 1 | ✅ |
| **Total** | **10** | ✅ |

## Rate Limiter Configuration (Production)

| Rate Limiter | Limit | Endpoints |
|-------------|-------|-----------|
| `auth` | 5/min | `/login` |
| `register` | 3/hour | `/register` |
| `email-action` | 3/hour | `/forgot-password`, `/resend-verification` |
| `sensitive-action` | 5/hour | `/reset-password` |
| `token-action` | 3/min | `/refresh`, `/verify-email` |

## Key Design Decisions

1. **카테고리 기반 Rate Limiter**: 유사한 리소스는 동일한 Rate Limiter 공유
2. **In-Memory 방식 유지**: 단일 서버 환경에 적합, 향후 Redis 전환 용이
3. **IP 기반 제한**: 비인증 엔드포인트 보호에 적합
4. **테스트 격리**: `TestableRateLimiterRegistry`로 테스트 간 상태 공유 방지

## Security Improvements

- ✅ Brute-force 공격 방지 (`/login` - 5회/분)
- ✅ 이메일 폭탄 공격 방지 (`/forgot-password`, `/resend-verification` - 3회/시간)
- ✅ 계정 생성 남용 방지 (`/register` - 3회/시간)
- ✅ 토큰 남용 방지 (`/refresh`, `/verify-email` - 3회/분)
- ✅ 에러 응답에 내부 정보 미노출

## Files Changed

```
src/main/java/platform/ecommerce/
├── controller/
│   └── AuthController.java (modified - added @RateLimiter)
└── exception/
    └── GlobalExceptionHandler.java (modified - added RequestNotPermitted handler)

src/main/resources/
└── application.yml (modified - added rate limiter instances)

src/test/java/platform/ecommerce/
├── controller/
│   └── AuthControllerRateLimiterTest.java (new)
└── config/
    └── TestRateLimiterConfig.java (new)

src/test/resources/
└── application-test.yml (modified - added test rate limiter config)

docs/auth-rate-limiter/
├── detail/plan.md
├── test/test-plan.md
└── checkpoint.md
```

## Resolved TODOs

From `docs/auth/checkpoint.md`:
- [x] ~~TODO: Add rate limiting for login attempts~~

## Future Considerations

- 분산 환경 스케일 아웃 시 Redis 기반 Rate Limiter 전환 필요
- Retry-After 헤더 추가 검토
- IP별 Rate Limiting 외에 User별 Rate Limiting 고려
