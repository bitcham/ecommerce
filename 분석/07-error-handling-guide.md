# 07. 에러 처리 가이드 (Error Handling Guide)

> **학습 목표**: 프로젝트의 예외 처리 전략을 이해하고, 일관된 방식으로 에러를 처리하는 방법을 학습합니다.

---

## 1. 에러 처리 아키텍처

### 1.1 전체 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        GlobalExceptionHandler                   │
│                    (모든 예외를 잡아서 변환)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────┐   ┌───────────────┐   ┌───────────────┐     │
│  │ BusinessException │  │ ValidationEx  │   │ Spring Security │   │
│  │                 │   │               │   │   Exception   │     │
│  └───────┬───────┘   └───────┬───────┘   └───────┬───────┘     │
│          │                   │                   │               │
│          ▼                   ▼                   ▼               │
│  ┌───────────────────────────────────────────────────────┐      │
│  │                    ErrorResponse                      │      │
│  │         { code, message, details? }                   │      │
│  └───────────────────────────────────────────────────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 예외 클래스 계층 구조

```
RuntimeException
    └── BusinessException (추상 기본 클래스)
            ├── EntityNotFoundException      (404 Not Found)
            ├── DuplicateResourceException   (409 Conflict)
            ├── InvalidStateException        (400 Bad Request)
            └── AuthenticationException      (401 Unauthorized)
```

---

## 2. ErrorCode 체계

### 2.1 에러 코드 네이밍 규칙

**참조**: `ErrorCode.java:13-111`

에러 코드는 **{도메인}_{에러타입}** 형식으로 정의합니다.

**코드 범위**:

| 범위 | 도메인 | 예시 |
|------|--------|------|
| 1xxx | Common (공통) | `INVALID_INPUT`, `RESOURCE_NOT_FOUND` |
| 2xxx | Auth (인증) | `TOKEN_EXPIRED`, `INVALID_CREDENTIALS` |
| 3xxx | Member (회원) | `MEMBER_NOT_FOUND`, `MEMBER_SUSPENDED` |
| 34xx | Seller (판매자) | `SELLER_NOT_FOUND`, `SELLER_NOT_APPROVED` |
| 4xxx | Product (상품) | `PRODUCT_NOT_FOUND`, `INSUFFICIENT_STOCK` |
| 45xx | Category (카테고리) | `CATEGORY_NOT_FOUND` |
| 5xxx | Order (주문) | `ORDER_NOT_FOUND`, `ORDER_CANNOT_CANCEL` |
| 6xxx | Payment (결제) | `PAYMENT_FAILED`, `REFUND_FAILED` |
| 7xxx | Cart (장바구니) | `CART_NOT_FOUND`, `CART_EMPTY` |
| 8xxx | Coupon (쿠폰) | `COUPON_EXPIRED`, `COUPON_ALREADY_USED` |
| 85xx | Review (리뷰) | `REVIEW_NOT_FOUND` |
| 9xxx | System (시스템) | `INTERNAL_ERROR`, `SERVICE_UNAVAILABLE` |

### 2.2 ErrorCode 구조

```java
// ErrorCode.java:13-111
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (1xxx)
    INVALID_INPUT(1001, HttpStatus.BAD_REQUEST, "Invalid input"),
    RESOURCE_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED(1003, HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN(1004, HttpStatus.FORBIDDEN, "Access forbidden"),

    // Member (3xxx)
    MEMBER_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "Member not found"),
    MEMBER_SUSPENDED(3002, HttpStatus.FORBIDDEN, "Member account is suspended"),
    MEMBER_ALREADY_WITHDRAWN(3003, HttpStatus.BAD_REQUEST, "Member already withdrawn"),

    // Product (4xxx)
    PRODUCT_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_SOLD_OUT(4002, HttpStatus.BAD_REQUEST, "Product is sold out"),
    INSUFFICIENT_STOCK(4003, HttpStatus.BAD_REQUEST, "Insufficient stock"),

    // System (9xxx)
    INTERNAL_ERROR(9001, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
```

---

## 3. 예외 클래스 상세

### 3.1 BusinessException (기본 클래스)

**참조**: `BusinessException.java:9-27`

모든 비즈니스 예외의 기본 클래스입니다.

```java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 메시지 사용
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 커스텀 메시지 사용
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // 원인 예외 포함
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
```

### 3.2 전문화된 예외 클래스들

#### EntityNotFoundException (404)

엔티티를 찾을 수 없을 때 사용합니다.

**참조**: `EntityNotFoundException.java:6-25`

```java
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // Static factory 메서드 - ID로 메시지 자동 생성
    public static EntityNotFoundException of(ErrorCode errorCode, Long id) {
        return new EntityNotFoundException(errorCode,
            String.format("%s with id %d", errorCode.getMessage(), id));
    }
}
```

**사용 예시**:
```java
// Service 계층에서
Member member = memberRepository.findById(memberId)
    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

// 커스텀 메시지 포함
Product product = productRepository.findById(productId)
    .orElseThrow(() -> new EntityNotFoundException(
        ErrorCode.PRODUCT_NOT_FOUND,
        "Product not found with id: " + productId));
```

#### DuplicateResourceException (409)

리소스 중복 시 사용합니다.

**참조**: `DuplicateResourceException.java:6-20`

```java
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // Static factory 메서드 - 필드와 값 정보 포함
    public static DuplicateResourceException of(ErrorCode errorCode, String field, String value) {
        return new DuplicateResourceException(errorCode,
            String.format("%s: %s = %s", errorCode.getMessage(), field, value));
    }
}
```

**사용 예시**:
```java
if (memberRepository.existsByEmail(email)) {
    throw new DuplicateResourceException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
}
```

#### InvalidStateException (400)

잘못된 상태 전이나 비즈니스 규칙 위반 시 사용합니다.

**참조**: `InvalidStateException.java:6-15`

```java
public class InvalidStateException extends BusinessException {

    public InvalidStateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidStateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
```

**사용 예시**:
```java
// 주문 취소 불가 상태
if (!order.canCancel()) {
    throw new InvalidStateException(ErrorCode.ORDER_CANNOT_CANCEL);
}

// 비밀번호 불일치
if (!password.equals(passwordConfirm)) {
    throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Passwords do not match");
}
```

#### AuthenticationException (401)

인증 관련 오류 시 사용합니다.

**참조**: `AuthenticationException.java`

```java
public class AuthenticationException extends BusinessException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

**사용 예시**:
```java
// JwtTokenProvider.java:92-115
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

## 4. GlobalExceptionHandler

### 4.1 전체 구조

**참조**: `GlobalExceptionHandler.java:27-212`

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) { ... }

    // 2. Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(...) { ... }

    // 3. 보안 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(...) { ... }

    // 4. 기타 Spring 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(...) { ... }

    // 5. 알 수 없는 예외 처리 (최후의 방어선)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) { ... }
}
```

### 4.2 비즈니스 예외 처리

```java
// GlobalExceptionHandler.java:32-42
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    log.warn("Business exception: {} - {}", e.getErrorCode(), e.getMessage());

    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse error = ErrorResponse.of(errorCode.name(), e.getMessage());

    return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(error));
}
```

**응답 예시**:
```json
{
    "success": false,
    "data": null,
    "error": {
        "code": "MEMBER_NOT_FOUND",
        "message": "Member not found"
    }
}
```

### 4.3 Validation 예외 처리

```java
// GlobalExceptionHandler.java:47-67
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException e) {
    log.warn("Validation exception: {}", e.getMessage());

    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> FieldError.of(
                    error.getField(),
                    error.getRejectedValue() != null ? error.getRejectedValue().toString() : "null",
                    error.getDefaultMessage()))
            .toList();

    ErrorResponse error = ErrorResponse.of(
            ErrorCode.INVALID_INPUT.name(),
            "Validation failed",
            fieldErrors);

    return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(error));
}
```

**응답 예시**:
```json
{
    "success": false,
    "data": null,
    "error": {
        "code": "INVALID_INPUT",
        "message": "Validation failed",
        "details": [
            {
                "field": "email",
                "value": "invalid-email",
                "reason": "must be a well-formed email address"
            },
            {
                "field": "password",
                "value": "123",
                "reason": "size must be between 8 and 20"
            }
        ]
    }
}
```

### 4.4 알 수 없는 예외 처리

```java
// GlobalExceptionHandler.java:200-211
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    // 상세 로그 (스택 트레이스 포함) - 서버 내부용
    log.error("Unexpected error occurred", e);

    // 클라이언트에게는 일반적인 메시지만 반환 (보안)
    ErrorResponse error = ErrorResponse.of(
            ErrorCode.INTERNAL_ERROR.name(),
            ErrorCode.INTERNAL_ERROR.getMessage());

    return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
            .body(ApiResponse.error(error));
}
```

**보안 포인트**: 예상치 못한 예외의 상세 정보(스택 트레이스, 내부 메시지)는 **절대로** 클라이언트에게 노출하지 않습니다.

---

## 5. ErrorResponse 구조

### 5.1 기본 구조

**참조**: `ErrorResponse.java:14-57`

```java
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;      // 에러 코드 (MEMBER_NOT_FOUND)
    private final String message;   // 에러 메시지
    private final List<FieldError> details;  // 필드별 상세 에러 (nullable)

    // 기본 에러
    public static ErrorResponse of(String code, String message) { ... }

    // 필드 에러 포함
    public static ErrorResponse of(String code, String message, List<FieldError> details) { ... }

    @Getter
    public static class FieldError {
        private final String field;   // 필드명
        private final String value;   // 입력된 값
        private final String reason;  // 에러 사유
    }
}
```

### 5.2 응답 형식

**일반 에러**:
```json
{
    "code": "ORDER_NOT_FOUND",
    "message": "Order not found"
}
```

**Validation 에러**:
```json
{
    "code": "INVALID_INPUT",
    "message": "Validation failed",
    "details": [
        {
            "field": "quantity",
            "value": "-1",
            "reason": "must be greater than 0"
        }
    ]
}
```

---

## 6. 예외 처리 Best Practices

### 6.1 언제 어떤 예외를 사용할까?

| 상황 | 예외 클래스 | ErrorCode 예시 |
|------|------------|---------------|
| DB에서 엔티티 못 찾음 | `EntityNotFoundException` | `MEMBER_NOT_FOUND` |
| 중복 데이터 존재 | `DuplicateResourceException` | `EMAIL_ALREADY_EXISTS` |
| 비즈니스 규칙 위반 | `InvalidStateException` | `ORDER_CANNOT_CANCEL` |
| 인증 실패 | `AuthenticationException` | `TOKEN_EXPIRED` |
| 권한 없음 | Spring `AccessDeniedException` | `FORBIDDEN` |
| 입력값 검증 실패 | `@Valid` + `MethodArgumentNotValidException` | `INVALID_INPUT` |

### 6.2 예외 발생 패턴

#### Pattern 1: Optional 처리

```java
// 권장
public Member getMember(Long id) {
    return memberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
}

// 비권장 - null 체크
public Member getMember(Long id) {
    Member member = memberRepository.findById(id).orElse(null);
    if (member == null) {
        throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
    }
    return member;
}
```

#### Pattern 2: 상태 검증

```java
public void cancelOrder(Long orderId) {
    Order order = getOrder(orderId);

    // 상태 검증을 도메인 메서드로 위임
    if (!order.canCancel()) {
        throw new InvalidStateException(ErrorCode.ORDER_CANNOT_CANCEL);
    }

    order.cancel();
}
```

#### Pattern 3: 중복 검사

```java
public void register(MemberCreateRequest request) {
    // 먼저 중복 검사
    if (memberRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
    }

    // 중복이 아니면 생성
    Member member = Member.create(request);
    memberRepository.save(member);
}
```

### 6.3 로깅 전략

```java
// 비즈니스 예외 - WARN 레벨 (예상된 에러)
log.warn("Business exception: {} - {}", e.getErrorCode(), e.getMessage());

// 시스템 예외 - ERROR 레벨 + 스택 트레이스
log.error("Unexpected error occurred", e);

// 민감 정보 로깅 금지
// ❌ log.warn("Login failed for password: {}", password);
// ✅ log.warn("Login failed for email: {}", email);
```

---

## 7. 클라이언트 연동 가이드

### 7.1 HTTP 상태 코드 매핑

| HTTP Status | 의미 | ErrorCode 예시 |
|-------------|------|---------------|
| 400 Bad Request | 잘못된 요청 | `INVALID_INPUT`, `ORDER_CANNOT_CANCEL` |
| 401 Unauthorized | 인증 필요/실패 | `TOKEN_EXPIRED`, `INVALID_CREDENTIALS` |
| 403 Forbidden | 권한 없음 | `FORBIDDEN`, `MEMBER_SUSPENDED` |
| 404 Not Found | 리소스 없음 | `MEMBER_NOT_FOUND`, `ORDER_NOT_FOUND` |
| 409 Conflict | 리소스 충돌 | `EMAIL_ALREADY_EXISTS` |
| 500 Internal Error | 서버 에러 | `INTERNAL_ERROR` |

### 7.2 클라이언트 에러 처리 예시

```typescript
// TypeScript 예시
interface ErrorResponse {
    code: string;
    message: string;
    details?: FieldError[];
}

async function handleApiError(response: Response) {
    const body = await response.json();
    const error = body.error as ErrorResponse;

    switch (response.status) {
        case 400:
            if (error.details) {
                // Validation 에러 - 필드별 처리
                showFieldErrors(error.details);
            } else {
                showToast(error.message);
            }
            break;
        case 401:
            // 토큰 만료 - 재인증
            if (error.code === 'TOKEN_EXPIRED') {
                await refreshToken();
            } else {
                redirectToLogin();
            }
            break;
        case 404:
            showNotFound(error.message);
            break;
        case 500:
            showSystemError();
            break;
    }
}
```

---

## 8. 새로운 에러 추가 가이드

### 8.1 체크리스트

1. [ ] 적절한 도메인 범위의 코드 번호 선택
2. [ ] ErrorCode enum에 추가
3. [ ] 적절한 예외 클래스 선택 (또는 새로 생성)
4. [ ] Service 계층에서 예외 발생
5. [ ] 테스트 코드 작성

### 8.2 예시: 새로운 에러 추가

**1단계: ErrorCode 추가**
```java
// ErrorCode.java
// Wishlist (87xx)
WISHLIST_NOT_FOUND(8701, HttpStatus.NOT_FOUND, "Wishlist item not found"),
WISHLIST_ALREADY_EXISTS(8702, HttpStatus.CONFLICT, "Product already in wishlist"),
WISHLIST_LIMIT_EXCEEDED(8703, HttpStatus.BAD_REQUEST, "Wishlist limit exceeded (max 100)");
```

**2단계: Service에서 사용**
```java
public void addToWishlist(Long memberId, Long productId) {
    // 중복 검사
    if (wishlistRepository.existsByMemberIdAndProductId(memberId, productId)) {
        throw new DuplicateResourceException(ErrorCode.WISHLIST_ALREADY_EXISTS);
    }

    // 한도 검사
    long count = wishlistRepository.countByMemberId(memberId);
    if (count >= 100) {
        throw new InvalidStateException(ErrorCode.WISHLIST_LIMIT_EXCEEDED);
    }

    // 추가
    wishlistRepository.save(new WishlistItem(memberId, productId));
}
```

**3단계: 테스트**
```java
@Test
@DisplayName("Should throw exception when wishlist limit exceeded")
void addToWishlist_limitExceeded_shouldThrowException() {
    // given
    given(wishlistRepository.existsByMemberIdAndProductId(anyLong(), anyLong()))
            .willReturn(false);
    given(wishlistRepository.countByMemberId(anyLong()))
            .willReturn(100L);

    // when & then
    assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 999L))
            .isInstanceOf(InvalidStateException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WISHLIST_LIMIT_EXCEEDED);
}
```

---

## 9. 학습 포인트

### 9.1 왜 이런 구조인가?

| 설계 결정 | 이유 |
|----------|------|
| 중앙 집중 예외 처리 | 일관된 응답 형식, 중복 제거, 유지보수 용이 |
| ErrorCode Enum | 타입 안전성, 자동완성, HTTP 상태 매핑 |
| BusinessException 계층 | 예외 분류, 처리 전략 다양화 |
| 클라이언트에 상세 정보 숨김 | 보안 (내부 구조 노출 방지) |

### 9.2 주의할 점

- **catch문에서 예외 무시 금지**
  ```java
  // ❌ 절대 금지
  try {
      doSomething();
  } catch (Exception e) {
      // 무시
  }

  // ✅ 최소한 로깅
  try {
      doSomething();
  } catch (Exception e) {
      log.error("Failed to do something", e);
      throw new BusinessException(ErrorCode.INTERNAL_ERROR);
  }
  ```

- **예외 체이닝 유지**
  ```java
  // ✅ 원인 예외 포함
  catch (IOException e) {
      throw new BusinessException(
          ErrorCode.FILE_UPLOAD_FAILED,
          "File upload failed",
          e  // 원인 포함
      );
  }
  ```

---

## 10. 다음 학습 내용

- `08-testing-guide.md`: 예외 테스트 방법 포함
- `09-security-checklist.md`: 보안 관련 에러 처리

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
