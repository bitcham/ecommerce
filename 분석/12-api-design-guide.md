# 12. API 설계 가이드 (API Design Guide)

> **학습 목표**: RESTful API 설계 원칙을 이해하고, 프로젝트의 API 컨벤션을 준수하는 방법을 학습합니다.

---

## 1. RESTful API 원칙

### 1.1 REST 핵심 개념

```
┌─────────────────────────────────────────────────────────────┐
│                      REST 원칙                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 리소스 중심        - URI는 명사로 (동사 X)                │
│  2. HTTP 메서드 활용   - GET, POST, PUT, PATCH, DELETE      │
│  3. 무상태성          - 각 요청은 독립적                     │
│  4. 일관된 응답 형식   - ApiResponse<T> 사용                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 HTTP 메서드 활용

| 메서드 | 용도 | 멱등성 | 예시 |
|--------|------|--------|------|
| `GET` | 조회 | ✅ | `GET /api/v1/members/1` |
| `POST` | 생성 | ❌ | `POST /api/v1/members` |
| `PUT` | 전체 수정 | ✅ | `PUT /api/v1/members/1` |
| `PATCH` | 부분 수정 | ✅ | `PATCH /api/v1/members/1` |
| `DELETE` | 삭제 | ✅ | `DELETE /api/v1/members/1` |

---

## 2. URI 설계 규칙

### 2.1 기본 규칙

```
✅ 올바른 예시
/api/v1/members              # 복수형 명사
/api/v1/members/1            # 특정 리소스
/api/v1/members/1/addresses  # 하위 리소스
/api/v1/orders?status=PENDING  # 쿼리 파라미터

❌ 잘못된 예시
/api/v1/getMember            # 동사 사용
/api/v1/member               # 단수형
/api/v1/members/getById/1    # 불필요한 동사
```

### 2.2 프로젝트 URI 구조

**참조**: `MemberController.java:22`, `SecurityConfig.java:31-45`

```
/api/v1/{도메인}              # 기본 CRUD
/api/v1/{도메인}/{id}         # 특정 리소스
/api/v1/{도메인}/{id}/{하위}  # 중첩 리소스
/api/v1/{도메인}/{id}/{액션}  # 특수 작업
```

**실제 예시**:
```
# 회원
/api/v1/members                    # 회원 목록, 생성
/api/v1/members/{id}               # 조회, 수정, 삭제
/api/v1/members/{id}/addresses     # 주소 목록, 추가
/api/v1/members/{id}/restore       # 탈퇴 회원 복구 (특수 액션)

# 상품
/api/v1/products                   # 상품 목록, 생성
/api/v1/products/{id}              # 조회, 수정
/api/v1/products/{id}/publish      # 상품 게시 (상태 변경)
/api/v1/products/{id}/options      # 옵션 관리
/api/v1/products/{id}/images       # 이미지 관리

# 주문
/api/v1/orders                     # 주문 목록, 생성
/api/v1/orders/{id}                # 조회
/api/v1/orders/{id}/cancel         # 주문 취소
/api/v1/orders/{id}/pay            # 결제 요청
```

### 2.3 버전 관리

```
/api/v1/...   # 현재 버전
/api/v2/...   # 다음 버전 (하위 호환성 보장 어려울 때)
```

---

## 3. 요청 (Request)

### 3.1 Request Body 구조

**참조**: `MemberCreateRequest.java`, `OrderCreateRequest.java`

```java
// 생성 요청 예시
public record MemberCreateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20)
    String password,

    @NotBlank
    String passwordConfirm,

    @NotBlank
    @Size(min = 2, max = 50)
    String name,

    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")
    String phone
) {
    // 비즈니스 검증 메서드
    public boolean isPasswordMatched() {
        return password.equals(passwordConfirm);
    }
}
```

### 3.2 수정 요청 (Partial Update)

```java
// PATCH용 - 모든 필드 nullable
public record MemberUpdateRequest(
    @Size(min = 2, max = 50)
    String name,      // null이면 수정 안 함

    String phone,     // null이면 수정 안 함

    String profileImage
) {}
```

### 3.3 검색 조건

**참조**: `MemberSearchCondition.java`, `OrderSearchCondition.java`

```java
// 검색 조건 DTO
@Builder
public record MemberSearchCondition(
    String email,
    String name,
    MemberStatus status,
    MemberRole role,
    Boolean excludeWithdrawn  // null이면 기본값 true 적용
) {
    public static MemberSearchCondition empty() {
        return MemberSearchCondition.builder().build();
    }
}
```

**사용**:
```
GET /api/v1/members?email=test&status=ACTIVE&page=0&size=20
```

---

## 4. 응답 (Response)

### 4.1 표준 응답 형식

**참조**: `ApiResponse.java:16-79`

```java
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;
    private final Meta meta;

    // Factory methods
    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> created(T data) { ... }
    public static <T> ApiResponse<T> error(ErrorResponse error) { ... }
}
```

### 4.2 성공 응답

**단일 리소스**:
```json
{
    "success": true,
    "data": {
        "id": 1,
        "email": "user@example.com",
        "name": "홍길동",
        "status": "ACTIVE"
    },
    "meta": {
        "timestamp": "2025-01-15T10:30:00",
        "requestId": "uuid-here"
    }
}
```

**목록 (페이징)**:
```json
{
    "success": true,
    "data": {
        "content": [
            { "id": 1, "email": "user1@example.com" },
            { "id": 2, "email": "user2@example.com" }
        ],
        "page": {
            "size": 20,
            "number": 0,
            "totalElements": 100,
            "totalPages": 5
        }
    },
    "meta": { ... }
}
```

### 4.3 에러 응답

**참조**: `ErrorResponse.java:14-57`

```json
{
    "success": false,
    "error": {
        "code": "MEMBER_NOT_FOUND",
        "message": "Member not found"
    },
    "meta": { ... }
}
```

**Validation 에러**:
```json
{
    "success": false,
    "error": {
        "code": "INVALID_INPUT",
        "message": "Validation failed",
        "details": [
            {
                "field": "email",
                "value": "invalid",
                "reason": "must be a well-formed email address"
            }
        ]
    },
    "meta": { ... }
}
```

---

## 5. HTTP 상태 코드

### 5.1 성공 응답

| 코드 | 상황 | 사용 예시 |
|------|------|----------|
| `200 OK` | 조회, 수정 성공 | GET, PATCH, PUT |
| `201 Created` | 생성 성공 | POST |
| `204 No Content` | 성공, 응답 본문 없음 | DELETE, 비밀번호 변경 |

### 5.2 클라이언트 에러

| 코드 | 상황 | ErrorCode |
|------|------|-----------|
| `400 Bad Request` | 잘못된 요청 | `INVALID_INPUT` |
| `401 Unauthorized` | 인증 필요/실패 | `TOKEN_EXPIRED` |
| `403 Forbidden` | 권한 없음 | `FORBIDDEN` |
| `404 Not Found` | 리소스 없음 | `MEMBER_NOT_FOUND` |
| `409 Conflict` | 리소스 충돌 | `EMAIL_ALREADY_EXISTS` |

### 5.3 서버 에러

| 코드 | 상황 |
|------|------|
| `500 Internal Server Error` | 예상치 못한 서버 오류 |
| `502 Bad Gateway` | 외부 서비스 오류 |
| `503 Service Unavailable` | 서비스 일시 중단 |

---

## 6. Controller 작성 패턴

### 6.1 기본 구조

**참조**: `MemberController.java:24-145`

```java
@Tag(name = "Member", description = "Member management API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 생성 (POST)
    @Operation(summary = "Register member")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> register(
            @Valid @RequestBody MemberCreateRequest request) {
        return ApiResponse.created(memberService.register(request));
    }

    // 목록 조회 (GET)
    @Operation(summary = "Search members")
    @GetMapping
    public ApiResponse<PageResponse<MemberResponse>> searchMembers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        // ...
    }

    // 단건 조회 (GET)
    @Operation(summary = "Get member detail")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberDetailResponse> getMember(
            @PathVariable Long memberId) {
        return ApiResponse.success(memberService.getMemberDetail(memberId));
    }

    // 수정 (PATCH)
    @Operation(summary = "Update profile")
    @PatchMapping("/{memberId}")
    public ApiResponse<MemberResponse> updateProfile(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberService.updateProfile(memberId, request));
    }

    // 삭제 (DELETE)
    @Operation(summary = "Withdraw member")
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long memberId) {
        memberService.withdraw(memberId);
    }
}
```

### 6.2 어노테이션 사용

| 어노테이션 | 용도 |
|-----------|------|
| `@Tag` | Swagger 그룹명 |
| `@Operation` | API 설명 |
| `@Parameter` | 파라미터 설명 |
| `@Valid` | 요청 검증 |
| `@ResponseStatus` | 응답 상태 코드 |
| `@PathVariable` | URI 경로 변수 |
| `@RequestParam` | 쿼리 파라미터 |
| `@RequestBody` | 요청 본문 |
| `@PageableDefault` | 기본 페이징 설정 |

---

## 7. 페이징 & 정렬

### 7.1 요청 파라미터

```
GET /api/v1/products?page=0&size=20&sort=createdAt,desc
```

| 파라미터 | 설명 | 기본값 |
|---------|------|--------|
| `page` | 페이지 번호 (0부터) | 0 |
| `size` | 페이지 크기 | 20 |
| `sort` | 정렬 기준 | createdAt,desc |

### 7.2 응답 구조

**참조**: `PageResponse.java`

```java
public class PageResponse<T> {
    private List<T> content;
    private PageInfo page;

    @Getter
    public static class PageInfo {
        private int size;           // 페이지 크기
        private int number;         // 현재 페이지 (0부터)
        private long totalElements; // 전체 개수
        private int totalPages;     // 전체 페이지 수
    }
}
```

---

## 8. API 문서화 (Swagger)

### 8.1 OpenAPI 설정

**참조**: `OpenApiConfig.java`

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce API")
                .version("1.0")
                .description("이커머스 플랫폼 REST API"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### 8.2 API 문서 접근

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

---

## 9. API 설계 체크리스트

### 9.1 URI 설계

- [ ] 복수형 명사 사용 (`/members`, `/orders`)
- [ ] 소문자, 하이픈 사용 (`/order-items`)
- [ ] 버전 포함 (`/api/v1/...`)
- [ ] 적절한 중첩 깊이 (최대 3단계)

### 9.2 요청/응답

- [ ] `@Valid`로 입력 검증
- [ ] 표준 응답 형식 (`ApiResponse<T>`)
- [ ] 적절한 HTTP 상태 코드
- [ ] 에러 응답에 충분한 정보

### 9.3 문서화

- [ ] Swagger 어노테이션 추가
- [ ] 파라미터 설명 명시
- [ ] 예시 응답 제공

---

## 10. 학습 포인트

### 10.1 왜 이런 규칙인가?

| 규칙 | 이유 |
|------|------|
| 복수형 명사 | 일관성, 컬렉션 개념 |
| 표준 응답 형식 | 클라이언트 파싱 용이 |
| HTTP 메서드 구분 | 의미 명확, 캐싱 가능 |
| 페이징 표준화 | 대용량 데이터 처리 |

### 10.2 주의할 점

- **민감 정보 응답 제외** (비밀번호, 토큰)
- **적절한 에러 메시지** (너무 상세한 정보는 보안 위험)
- **버전 호환성** (기존 API 변경 시 주의)

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
