# 09. 보안 체크리스트 (Security Checklist)

> **학습 목표**: 웹 애플리케이션 보안의 핵심 원칙을 이해하고, 안전한 코드를 작성하는 방법을 학습합니다.

---

## 1. 보안 아키텍처 개요

### 1.1 인증/인가 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Request                          │
└───────────────────────────────┬─────────────────────────────────┘
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                  JwtAuthenticationFilter                       │
│    - Token 추출 및 검증                                         │
│    - SecurityContext에 인증 정보 설정                           │
└───────────────────────────────┬───────────────────────────────┘
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                     SecurityFilterChain                        │
│    - URL 기반 권한 체크 (hasRole, authenticated)                │
│    - 공개 엔드포인트 허용                                       │
└───────────────────────────────┬───────────────────────────────┘
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                  @PreAuthorize (Method Security)               │
│    - 메서드 레벨 권한 체크                                      │
└───────────────────────────────┬───────────────────────────────┘
                                ▼
┌───────────────────────────────────────────────────────────────┐
│                         Controller                             │
└───────────────────────────────────────────────────────────────┘
```

### 1.2 주요 보안 컴포넌트

| 컴포넌트 | 역할 | 참조 |
|---------|------|------|
| `SecurityConfig` | 보안 설정 중앙화 | `SecurityConfig.java:26-118` |
| `JwtTokenProvider` | JWT 생성/검증 | `JwtTokenProvider.java:21-130` |
| `JwtAuthenticationFilter` | 요청별 인증 처리 | `JwtAuthenticationFilter.java:27-69` |
| `PasswordEncoder` | 비밀번호 암호화 | BCrypt 사용 |

---

## 2. 인증 (Authentication)

### 2.1 JWT 토큰 구조

**참조**: `JwtTokenProvider.java:38-51`

```java
public String generateAccessToken(Member member) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
            .subject(member.getId().toString())  // 사용자 식별자
            .claim("email", member.getEmail())   // 이메일
            .claim("role", member.getRole().name())  // 권한
            .issuer(issuer)                      // 발급자
            .issuedAt(now)                       // 발급 시간
            .expiration(expiryDate)              // 만료 시간
            .signWith(secretKey)                 // 서명
            .compact();
}
```

### 2.2 보안 체크리스트 - 인증

| 항목 | 체크 | 설명 |
|------|------|------|
| ☑️ | JWT Secret 환경변수 사용 | 코드에 하드코딩 금지 |
| ☑️ | 적절한 토큰 만료 시간 | Access: 1시간, Refresh: 7일 |
| ☑️ | BCrypt 비밀번호 해싱 | 평문 저장 금지 |
| ☑️ | Stateless 세션 관리 | CSRF 방지 |
| ☑️ | 토큰 검증 예외 처리 | 만료, 변조 등 |

### 2.3 비밀번호 정책

**권장 정책**:
```java
// DTO에서 검증
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
    message = "Password must contain at least one uppercase, lowercase, number, and special character"
)
private String password;
```

| 요구사항 | 최소값 |
|---------|-------|
| 최소 길이 | 8자 |
| 대문자 | 1개 이상 |
| 소문자 | 1개 이상 |
| 숫자 | 1개 이상 |
| 특수문자 | 1개 이상 |

---

## 3. 인가 (Authorization)

### 3.1 역할 기반 접근 제어 (RBAC)

**참조**: `SecurityConfig.java:58-109`

```java
.authorizeHttpRequests(auth -> auth
    // 공개 엔드포인트
    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
    .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()

    // Admin 전용
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/api/v1/orders").hasRole("ADMIN")

    // Seller 권한
    .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAnyRole("SELLER", "ADMIN")
    .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")

    // 인증 필요
    .requestMatchers("/api/v1/members/me/**").authenticated()
    .requestMatchers("/api/v1/cart/**").authenticated()

    // 기본: 인증 필요
    .anyRequest().authenticated()
)
```

### 3.2 역할 계층

```
ADMIN
  └── SELLER
        └── CUSTOMER
```

| 역할 | 권한 |
|------|------|
| `CUSTOMER` | 구매, 리뷰 작성, 장바구니 |
| `SELLER` | CUSTOMER + 상품 등록/관리 |
| `ADMIN` | SELLER + 회원 관리, 주문 조회, 쿠폰 관리 |

### 3.3 보안 체크리스트 - 인가

| 항목 | 체크 | 설명 |
|------|------|------|
| ☑️ | 최소 권한 원칙 | 필요한 최소한의 권한만 부여 |
| ☑️ | URL 패턴 순서 | 구체적인 패턴을 먼저 정의 |
| ☑️ | 기본 거부 정책 | `anyRequest().authenticated()` |
| ☑️ | 소유권 검증 | 본인 리소스만 접근 가능 |

### 3.4 소유권 검증 예시

```java
// Service 레벨에서 소유권 검증
public OrderResponse getOrder(Long memberId, Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

    // 본인 주문인지 확인
    if (!order.getMember().getId().equals(memberId)) {
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    return orderMapper.toResponse(order);
}

// Controller에서 현재 사용자 ID 전달
@GetMapping("/orders/{orderId}")
public ResponseEntity<?> getOrder(
        @AuthenticationPrincipal Long memberId,  // JWT에서 추출
        @PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrder(memberId, orderId));
}
```

---

## 4. OWASP Top 10 대응

### 4.1 A01: Broken Access Control (접근 제어 실패)

**위험 코드**:
```java
// ❌ 인가 없이 모든 사용자 데이터 반환
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}
```

**안전한 코드**:
```java
// ✅ 본인 확인 후 반환
@GetMapping("/users/{id}")
public User getUser(
        @AuthenticationPrincipal Long currentUserId,
        @PathVariable Long id) {
    if (!currentUserId.equals(id)) {
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    return userRepository.findById(id).orElseThrow();
}
```

### 4.2 A02: Cryptographic Failures (암호화 실패)

**체크리스트**:
| 항목 | 상태 | 설명 |
|------|------|------|
| ☑️ | HTTPS 사용 | 전송 중 암호화 |
| ☑️ | BCrypt 비밀번호 | 저장 시 해싱 |
| ☑️ | JWT 비밀키 보호 | 환경변수로 관리 |
| ☑️ | 민감정보 로그 제외 | 비밀번호, 토큰 |

**안전한 로깅**:
```java
// ❌ 절대 금지
log.info("Login attempt: email={}, password={}", email, password);

// ✅ 안전
log.info("Login attempt: email={}", email);
```

### 4.3 A03: Injection (인젝션)

#### SQL Injection 방지

**위험 코드**:
```java
// ❌ 직접 문자열 연결 - SQL Injection 취약
String query = "SELECT * FROM users WHERE email = '" + email + "'";
```

**안전한 코드**:
```java
// ✅ JPA/QueryDSL 사용 - 파라미터 바인딩
@Query("SELECT m FROM Member m WHERE m.email = :email")
Optional<Member> findByEmail(@Param("email") String email);

// ✅ QueryDSL
return queryFactory
    .selectFrom(member)
    .where(member.email.eq(email))  // 자동 파라미터 바인딩
    .fetchOne();
```

#### XSS 방지

**위험 코드**:
```java
// ❌ 사용자 입력을 그대로 반환
return "<div>" + userInput + "</div>";
```

**안전한 코드**:
```java
// ✅ JSON 응답 사용 (자동 이스케이프)
return ApiResponse.success(data);

// ✅ 필요시 HTML 이스케이프
import org.springframework.web.util.HtmlUtils;
String safe = HtmlUtils.htmlEscape(userInput);
```

### 4.4 A04: Insecure Design (안전하지 않은 설계)

**설계 체크리스트**:
| 항목 | 설명 |
|------|------|
| 비즈니스 로직 검증 | 할인율 범위, 수량 제한 등 |
| Rate Limiting | API 호출 제한 |
| 계정 잠금 | 로그인 실패 시 잠금 |
| 재고 동시성 | 재고 차감 시 동시성 제어 |

### 4.5 A05: Security Misconfiguration (보안 설정 오류)

**참조**: `SecurityConfig.java:53-116`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ✅ CSRF 비활성화 (Stateless API)
        .csrf(AbstractHttpConfigurer::disable)

        // ✅ Stateless 세션
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // ✅ Form Login 비활성화
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        // ✅ JWT 필터 추가
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### 4.6 A07: Identification and Authentication Failures

**보안 조치**:
```java
// ✅ 토큰 검증 실패 시 적절한 응답
private Claims parseToken(String token) {
    try {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    } catch (ExpiredJwtException e) {
        log.warn("JWT token is expired: {}", e.getMessage());
        throw new AuthenticationException(ErrorCode.TOKEN_EXPIRED);
    } catch (MalformedJwtException e) {
        log.warn("JWT token is malformed: {}", e.getMessage());
        throw new AuthenticationException(ErrorCode.TOKEN_INVALID);
    }
}
```

---

## 5. 입력값 검증

### 5.1 DTO 레벨 검증

```java
public class MemberCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    private String name;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Invalid phone format")
    private String phone;
}
```

### 5.2 Controller 검증

```java
@PostMapping
public ResponseEntity<?> register(
        @Valid @RequestBody MemberCreateRequest request) {  // @Valid 필수
    return ResponseEntity.ok(memberService.register(request));
}
```

### 5.3 비즈니스 로직 검증

```java
// Service 레벨 추가 검증
public void processOrder(OrderCreateRequest request) {
    // 재고 검증
    for (OrderItemRequest item : request.getItems()) {
        Product product = getProduct(item.getProductId());
        if (product.getStock() < item.getQuantity()) {
            throw new InvalidStateException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    // 가격 검증 (클라이언트 조작 방지)
    Money serverCalculatedTotal = calculateTotal(request.getItems());
    if (!serverCalculatedTotal.equals(request.getTotalAmount())) {
        throw new InvalidStateException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
}
```

---

## 6. 에러 응답 보안

### 6.1 정보 노출 방지

**참조**: `GlobalExceptionHandler.java:200-211`

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    // ✅ 상세 로그는 서버에만
    log.error("Unexpected error occurred", e);

    // ✅ 클라이언트에게는 일반 메시지만
    ErrorResponse error = ErrorResponse.of(
            ErrorCode.INTERNAL_ERROR.name(),
            "Internal server error"  // 스택 트레이스 노출 금지
    );

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(error));
}
```

### 6.2 노출 금지 정보

| 노출 금지 | 이유 |
|----------|------|
| 스택 트레이스 | 내부 구조 노출 |
| SQL 쿼리 | 테이블/컬럼 구조 노출 |
| 파일 경로 | 서버 구조 노출 |
| 버전 정보 | 취약점 타겟팅 |
| 상세 에러 메시지 | 공격 힌트 제공 |

---

## 7. 민감 데이터 보호

### 7.1 응답에서 제외

```java
// DTO에서 민감 필드 제외
public record MemberResponse(
    Long id,
    String email,
    String name,
    MemberStatus status
    // password 필드 없음!
) {}
```

### 7.2 로깅에서 제외

```java
// ❌ 금지
log.debug("Request: {}", request);  // 전체 객체 로깅

// ✅ 안전
log.debug("Processing request for memberId: {}", request.getMemberId());
```

### 7.3 Masking 적용

```java
// 부분 마스킹
public String maskEmail(String email) {
    int atIndex = email.indexOf('@');
    if (atIndex <= 3) return "***" + email.substring(atIndex);
    return email.substring(0, 3) + "***" + email.substring(atIndex);
}

// 예: test@example.com → tes***@example.com
```

---

## 8. 보안 테스트

### 8.1 인증 테스트

```java
@Test
@DisplayName("Should reject expired token")
void expiredToken_shouldReturn401() {
    String expiredToken = generateExpiredToken();

    assertThatThrownBy(() -> jwtTokenProvider.parseToken(expiredToken))
        .isInstanceOf(AuthenticationException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.TOKEN_EXPIRED);
}

@Test
@DisplayName("Should reject tampered token")
void tamperedToken_shouldReturn401() {
    String tamperedToken = validToken + "tampered";

    assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
}
```

### 8.2 인가 테스트

```java
@Test
@DisplayName("Customer should not access admin endpoint")
void customer_accessAdminEndpoint_shouldReturn403() {
    // given
    Member customer = createActiveMember();  // CUSTOMER role
    String token = jwtTokenProvider.generateAccessToken(customer);

    // when & then
    mockMvc.perform(get("/api/v1/admin/members")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
}
```

---

## 9. 개발 시 보안 체크리스트

### 9.1 코드 작성 전

- [ ] 민감 데이터가 포함되는가?
- [ ] 권한 검증이 필요한가?
- [ ] 입력값 검증이 충분한가?

### 9.2 코드 작성 후

- [ ] SQL Injection 취약점이 없는가?
- [ ] XSS 취약점이 없는가?
- [ ] 소유권 검증이 되어 있는가?
- [ ] 에러 메시지에 민감 정보가 없는가?
- [ ] 로그에 민감 정보가 없는가?

### 9.3 배포 전

- [ ] 환경변수로 시크릿 관리
- [ ] HTTPS 적용
- [ ] 디버그 모드 비활성화
- [ ] 상세 에러 응답 비활성화

---

## 10. 참고 자료

### 10.1 관련 파일 위치

| 파일 | 경로 |
|------|------|
| 보안 설정 | `config/SecurityConfig.java` |
| JWT Provider | `security/JwtTokenProvider.java` |
| JWT Filter | `security/JwtAuthenticationFilter.java` |
| JWT Properties | `config/JwtProperties.java` |
| 에러 핸들러 | `exception/GlobalExceptionHandler.java` |

### 10.2 외부 참고 자료

- [OWASP Top 10](https://owasp.org/Top10/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
