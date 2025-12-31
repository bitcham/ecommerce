# 티켓 1: Auth API Rate Limiting 적용

**Priority**: P2 (Medium)
**Estimate**: 1시간
**Type**: Security Enhancement
**담당**: 주니어 개발자

---

## 배경

Lead Engineer 코드 리뷰에서 **보안 취약점**이 발견되었습니다.
현재 인증 관련 API에 **Rate Limiting이 적용되어 있지 않아** Brute Force 공격에 취약합니다.

> "Login and register endpoints lack explicit rate limiting. While Resilience4j is included as dependency, no `@RateLimiter` annotations are visible on auth endpoints."
> — Lead Engineer Review

---

## 문제점

### 현재 상태

**파일**: `AuthController.java`

```java
// 현재 코드 (취약)
@PostMapping("/login")
public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, ...) {
    // Rate Limiting 없음!
    // 공격자가 무한히 로그인 시도 가능
}

@PostMapping("/register")
public ApiResponse<MemberResponse> register(@Valid @RequestBody MemberCreateRequest request) {
    // Rate Limiting 없음!
    // 스팸 계정 대량 생성 가능
}
```

### 공격 시나리오

1. **Brute Force Attack**: 비밀번호를 무차별 대입하여 계정 탈취
2. **Credential Stuffing**: 유출된 ID/PW 목록으로 대량 로그인 시도
3. **Account Enumeration**: 이메일 존재 여부 확인 후 타겟 공격
4. **Spam Registration**: 대량의 가짜 계정 생성

---

## 해야 할 일

### 1단계: application.yml 설정 확인

이미 Resilience4j 설정이 있습니다:

```yaml
# application.yml (line 127-133)
resilience4j:
  ratelimiter:
    instances:
      default:
        limit-for-period: 100
        limit-refresh-period: 1s
```

**Auth 전용 Rate Limiter를 추가하세요:**

```yaml
resilience4j:
  ratelimiter:
    instances:
      default:
        limit-for-period: 100
        limit-refresh-period: 1s

      # 추가: 인증 API용 Rate Limiter
      authRateLimiter:
        limit-for-period: 5          # 5회
        limit-refresh-period: 1m     # 1분당
        timeout-duration: 0s         # 초과 시 즉시 거부

      # 추가: 회원가입용 Rate Limiter
      registerRateLimiter:
        limit-for-period: 3          # 3회
        limit-refresh-period: 1h     # 1시간당
        timeout-duration: 0s
```

### 2단계: AuthController에 Rate Limiter 적용

**파일**: `AuthController.java`

```java
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login")
    @RateLimiter(name = "authRateLimiter")  // 추가!
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse response = authService.login(request, httpRequest);
        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    @RateLimiter(name = "registerRateLimiter")  // 추가!
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> register(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response = authService.register(request);
        return ApiResponse.created(response);
    }

    @PostMapping("/forgot-password")
    @RateLimiter(name = "authRateLimiter")  // 추가!
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authService.requestPasswordReset(request.email());
    }

    @PostMapping("/resend-verification")
    @RateLimiter(name = "authRateLimiter")  // 추가!
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authService.resendVerificationEmail(request.email());
    }
}
```

### 3단계: Rate Limit 초과 시 에러 처리

**파일**: `GlobalExceptionHandler.java`

Rate Limit 초과 예외 처리를 추가하세요:

```java
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@ExceptionHandler(RequestNotPermitted.class)
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public ErrorResponse handleRateLimitExceeded(RequestNotPermitted ex) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());
    return ErrorResponse.of(
        ErrorCode.RATE_LIMIT_EXCEEDED.name(),
        ErrorCode.RATE_LIMIT_EXCEEDED.getMessage()
    );
}
```

**ErrorCode 확인**: 이미 `RATE_LIMIT_EXCEEDED`가 존재합니다 (line 105):

```java
// ErrorCode.java - 이미 존재함, 추가 불필요!
RATE_LIMIT_EXCEEDED(9004, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded")
```

---

## 적용할 엔드포인트

| 엔드포인트 | Rate Limiter | 제한 |
|-----------|--------------|------|
| `POST /login` | authRateLimiter | 5회/분 |
| `POST /register` | registerRateLimiter | 3회/시간 |
| `POST /forgot-password` | authRateLimiter | 5회/분 |
| `POST /resend-verification` | authRateLimiter | 5회/분 |

---

## 테스트 작성

**파일**: `test/java/platform/ecommerce/controller/AuthControllerRateLimitTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 5회 초과 시 429 반환")
    void login_exceedsRateLimit_returns429() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        String json = objectMapper.writeValueAsString(request);

        // 5회까지는 성공 (또는 401)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));
        }

        // 6회째는 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("회원가입 3회 초과 시 429 반환")
    void register_exceedsRateLimit_returns429() throws Exception {
        // given
        for (int i = 0; i < 3; i++) {
            MemberCreateRequest request = new MemberCreateRequest(
                "test" + i + "@test.com", "Password1!", "테스트" + i
            );
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // when & then - 4회째는 429
        MemberCreateRequest request = new MemberCreateRequest(
            "test99@test.com", "Password1!", "테스트"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }
}
```

---

## 수용 기준 (Acceptance Criteria)

- [ ] `application.yml`에 `authRateLimiter`, `registerRateLimiter` 설정 추가
- [ ] `POST /login`에 Rate Limiter 적용 (5회/분)
- [ ] `POST /register`에 Rate Limiter 적용 (3회/시간)
- [ ] `POST /forgot-password`에 Rate Limiter 적용
- [ ] `POST /resend-verification`에 Rate Limiter 적용
- [ ] Rate Limit 초과 시 `429 Too Many Requests` 응답
- [ ] GlobalExceptionHandler에 `RequestNotPermitted` 처리 추가
- [ ] 테스트 코드 작성 및 통과 (최소 2개)

---

## 수정/생성할 파일 목록

| 파일 | 타입 | 설명 |
|------|------|------|
| `application.yml` | MODIFY | Rate Limiter 설정 추가 |
| `AuthController.java` | MODIFY | @RateLimiter 어노테이션 추가 |
| `GlobalExceptionHandler.java` | MODIFY | RequestNotPermitted 예외 처리 |
| `ErrorCode.java` | 수정 불필요 | `RATE_LIMIT_EXCEEDED` 이미 존재 |
| `AuthControllerRateLimitTest.java` | NEW | Rate Limit 테스트 |

---

## 학습 포인트

1. **Rate Limiting**: API 보안의 첫 번째 방어선
2. **Resilience4j**: Spring Boot에서 Circuit Breaker, Rate Limiter 구현
3. **429 Too Many Requests**: HTTP 상태 코드의 의미
4. **Brute Force 방어**: 인증 시스템 보안의 기본

---

## 참고 자료

### Resilience4j 어노테이션

```java
@RateLimiter(name = "myLimiter", fallbackMethod = "fallback")
public String myMethod() { ... }

public String fallback(RequestNotPermitted ex) {
    return "Rate limit exceeded";
}
```

### 설정 옵션

| 옵션 | 설명 | 예시 |
|------|------|------|
| `limit-for-period` | 기간당 허용 요청 수 | 5 |
| `limit-refresh-period` | 기간 갱신 주기 | 1m, 1h |
| `timeout-duration` | 대기 시간 (0=즉시 거부) | 0s |

---

## 주의사항

- 테스트 시 Rate Limiter가 다른 테스트에 영향을 줄 수 있음 → `@DirtiesContext` 고려
- IP 기반 제한이 아닌 **요청 기반 제한**임을 이해
- 프로덕션에서는 Redis 기반 분산 Rate Limiter 고려 (현재 범위 외)
