# 06. 아키텍처 개요 (Architecture Overview)

> **학습 목표**: 이커머스 프로젝트의 전체 아키텍처를 이해하고, 각 계층의 역할과 데이터 흐름을 파악합니다.

---

## 1. 전체 아키텍처

### 1.1 레이어드 아키텍처 (Layered Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │    Filter    │  │   Advice     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Service    │  │    Mapper    │  │     DTO      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Entity    │  │ Value Object │  │     Enum     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Repository  │  │    Config    │  │   External   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 패키지 구조

```
platform.ecommerce/
├── config/           # 설정 클래스 (Security, JPA, Web 등)
├── controller/       # REST API 엔드포인트
├── domain/           # 도메인 엔티티 및 비즈니스 로직
│   ├── common/       # 공통 엔티티, Value Object
│   │   └── vo/       # Value Objects (Money, Address, Email 등)
│   ├── member/       # 회원 도메인
│   ├── product/      # 상품 도메인
│   ├── order/        # 주문 도메인
│   ├── cart/         # 장바구니 도메인
│   ├── payment/      # 결제 도메인
│   ├── category/     # 카테고리 도메인
│   ├── coupon/       # 쿠폰 도메인
│   ├── review/       # 리뷰 도메인
│   └── auth/         # 인증 관련 엔티티
├── dto/              # 데이터 전송 객체
│   ├── request/      # 요청 DTO
│   └── response/     # 응답 DTO
├── exception/        # 예외 클래스
├── mapper/           # Entity-DTO 변환
├── repository/       # 데이터 접근 계층
├── security/         # 보안 관련 (JWT, Filter)
└── service/          # 비즈니스 로직 서비스
```

---

## 2. 계층별 역할과 책임

### 2.1 Presentation Layer (표현 계층)

**역할**: 클라이언트 요청을 받아 적절한 서비스로 전달하고, 응답을 반환합니다.

**주요 컴포넌트**:

| 컴포넌트 | 역할 | 예시 파일 |
|---------|------|----------|
| Controller | HTTP 요청/응답 처리 | `MemberController.java:24` |
| Filter | 요청 전처리 (인증) | `JwtAuthenticationFilter.java:27` |
| ControllerAdvice | 전역 예외 처리 | `GlobalExceptionHandler.java:27` |

**코드 예시** - `MemberController.java:24-36`:
```java
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> register(
            @Valid @RequestBody MemberCreateRequest request) {
        MemberResponse response = memberService.register(request);
        return ApiResponse.created(response);
    }
}
```

**핵심 원칙**:
- Controller는 비즈니스 로직을 포함하지 않음
- 입력 유효성 검증은 `@Valid`와 DTO에서 처리
- 응답 형식은 `ApiResponse<T>`로 통일

---

### 2.2 Application Layer (응용 계층)

**역할**: 비즈니스 유스케이스를 구현하고, 트랜잭션을 관리합니다.

**주요 컴포넌트**:

| 컴포넌트 | 역할 | 예시 파일 |
|---------|------|----------|
| Service Interface | 서비스 계약 정의 | `MemberService.java:10-112` |
| Service Implementation | 비즈니스 로직 구현 | `MemberServiceImpl.java:28` |
| Mapper | Entity ↔ DTO 변환 | `MemberMapper.java:17-41` |

**코드 예시** - `MemberServiceImpl.java:34-53`:
```java
@Override
@Transactional
public MemberResponse register(MemberCreateRequest request) {
    log.info("Registering new member with email: {}", request.email());

    // 1. 비밀번호 확인 검증
    validatePasswordMatch(request);
    // 2. 이메일 중복 검사
    validateEmailNotExists(request.email());

    // 3. 회원 엔티티 생성 (Builder 패턴)
    Member member = Member.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .phone(request.phone())
            .build();

    // 4. 저장 및 응답 반환
    Member savedMember = memberRepository.save(member);
    return memberMapper.toResponse(savedMember);
}
```

**핵심 원칙**:
- `@Transactional`로 트랜잭션 경계 설정
- 읽기 전용 작업은 `@Transactional(readOnly = true)` 사용
- 예외는 비즈니스 예외 클래스 사용 (`BusinessException` 계열)

---

### 2.3 Domain Layer (도메인 계층)

**역할**: 핵심 비즈니스 규칙과 상태를 캡슐화합니다.

**주요 컴포넌트**:

| 컴포넌트 | 역할 | 예시 파일 |
|---------|------|----------|
| Entity | 도메인 객체, 비즈니스 로직 | `Member.java`, `Order.java` |
| Value Object | 불변 값 객체 | `Money.java:21`, `Address.java` |
| Enum | 상태/타입 정의 | `MemberStatus.java`, `OrderStatus.java` |
| Base Entity | 공통 필드 상속 | `BaseEntity.java:19` |

**Value Object 예시** - `Money.java:21-98`:
```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    // 팩토리 메서드
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return new Money(amount);
    }

    // 비즈니스 메서드 - 불변성 유지
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
}
```

**핵심 원칙**:
- Entity는 **Rich Domain Model** 패턴 적용 (비즈니스 로직 포함)
- Value Object는 **불변(Immutable)**으로 설계
- `BaseEntity`를 상속하여 공통 필드(id, createdAt, updatedAt) 관리

---

### 2.4 Infrastructure Layer (인프라 계층)

**역할**: 외부 시스템과의 통신, 데이터 저장소 접근을 담당합니다.

**주요 컴포넌트**:

| 컴포넌트 | 역할 | 예시 파일 |
|---------|------|----------|
| Repository | 데이터 접근 | `MemberRepository.java` |
| QueryRepository | 복잡한 쿼리 | `MemberQueryRepositoryImpl.java` |
| Config | 설정 빈 정의 | `SecurityConfig.java:26` |

**Repository 패턴**:
```java
// 기본 CRUD - Spring Data JPA
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
}

// 복잡한 쿼리 - QueryDSL
public interface MemberQueryRepository {
    Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable);
}
```

---

## 3. 데이터 흐름

### 3.1 요청-응답 흐름

```
┌──────────┐    ┌────────────┐    ┌───────────┐    ┌────────────┐
│  Client  │───▶│ Controller │───▶│  Service  │───▶│ Repository │
└──────────┘    └────────────┘    └───────────┘    └────────────┘
                      │                 │                 │
                      ▼                 ▼                 ▼
                ┌──────────┐      ┌──────────┐      ┌──────────┐
                │ Request  │      │  Domain  │      │ Database │
                │   DTO    │      │  Entity  │      │          │
                └──────────┘      └──────────┘      └──────────┘
```

### 3.2 DTO 변환 흐름

```
Request DTO ─────▶ Service ─────▶ Entity ─────▶ Service ─────▶ Response DTO
                      │                              │
                      └──────── Mapper 사용 ─────────┘
```

**Mapper 예시** - `MemberMapper.java`:
```java
@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberResponse toResponse(Member member);

    MemberDetailResponse toDetailResponse(Member member);

    AddressResponse toAddressResponse(MemberAddress address);
}
```

---

## 4. 공통 컴포넌트

### 4.1 BaseEntity

모든 엔티티가 상속하는 기본 클래스입니다.

**참조**: `BaseEntity.java:19-45`

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // equals, hashCode - ID 기반
}
```

### 4.2 SoftDeletable 인터페이스

논리적 삭제를 지원하는 엔티티를 위한 인터페이스입니다.

**참조**: `SoftDeletable.java`

```java
public interface SoftDeletable {
    boolean isDeleted();
    LocalDateTime getDeletedAt();
    void delete();
    void restore();
}
```

### 4.3 ApiResponse 표준 응답

모든 API 응답의 표준 형식입니다.

**참조**: `ApiResponse.java:16-79`

```java
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;
    private final Meta meta;  // 타임스탬프, requestId 포함

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> created(T data) { ... }  // 201 응답용
    public static <T> ApiResponse<T> error(ErrorResponse error) { ... }
}
```

**성공 응답 예시**:
```json
{
    "success": true,
    "data": {
        "id": 1,
        "email": "user@example.com",
        "name": "홍길동"
    },
    "meta": {
        "timestamp": "2025-01-15T10:30:00",
        "requestId": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**실패 응답 예시**:
```json
{
    "success": false,
    "error": {
        "code": "MEMBER_NOT_FOUND",
        "message": "Member not found"
    },
    "meta": {
        "timestamp": "2025-01-15T10:30:00",
        "requestId": "550e8400-e29b-41d4-a716-446655440001"
    }
}
```

---

## 5. 설정 파일 가이드

### 5.1 주요 설정 클래스

| 클래스 | 역할 | 참조 |
|-------|------|------|
| `SecurityConfig` | 보안 및 인증 설정 | `SecurityConfig.java:26-117` |
| `JpaConfig` | JPA Auditing 활성화 | `JpaConfig.java:1-25` |
| `WebConfig` | CORS, Interceptor 설정 | `WebConfig.java:1-24` |
| `OpenApiConfig` | Swagger/OpenAPI 설정 | `OpenApiConfig.java:1-73` |
| `JwtProperties` | JWT 설정값 바인딩 | `JwtProperties.java:1-32` |

### 5.2 Properties 바인딩 패턴

**참조**: `JwtProperties.java:1-32`

```java
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiration = 3600000;   // 1 hour
    private long refreshTokenExpiration = 604800000; // 7 days
    private String issuer = "ecommerce-api";
}
```

**application.yml**:
```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-key-that-should-be-at-least-32-characters}
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000
  issuer: ecommerce-api
```

---

## 6. 아키텍처 결정 기록 (ADR)

### ADR-001: 레이어드 아키텍처 선택

**맥락**: 이커머스 프로젝트의 기본 아키텍처 결정

**결정**: 전통적인 레이어드 아키텍처 채택

**이유**:
- 팀의 학습 곡선이 낮음
- 명확한 책임 분리
- Spring Boot와의 자연스러운 통합

**대안 검토**:
- 헥사고날 아키텍처: 복잡도 대비 초기 프로젝트에 과도함
- CQRS: 현재 규모에서 불필요한 복잡성

---

### ADR-002: Rich Domain Model 적용

**맥락**: Entity에 비즈니스 로직 포함 여부

**결정**: Rich Domain Model 패턴 적용

**예시**:
```java
// Member.java - 도메인 로직이 Entity 내부에 존재
public void withdraw() {
    if (this.status == MemberStatus.WITHDRAWN) {
        throw new InvalidStateException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }
    this.status = MemberStatus.WITHDRAWN;
    this.delete();  // Soft delete
}
```

**이점**:
- 도메인 로직의 응집도 향상
- 불변식(Invariant) 보장
- 테스트 용이성

---

### ADR-003: Interface-Implementation 분리

**맥락**: Service 계층의 구조 결정

**결정**: Service Interface와 Implementation 분리

**구조**:
```
service/
├── MemberService.java        // Interface
├── MemberServiceImpl.java    // Implementation
├── product/
│   ├── ProductService.java
│   └── ProductServiceImpl.java
```

**이점**:
- 테스트 시 Mock 주입 용이
- DIP(의존성 역전 원칙) 준수
- 구현체 교체 유연성

---

## 7. 학습 포인트

### 7.1 왜 이런 구조인가?

| 설계 결정 | 이유 |
|----------|------|
| DTO와 Entity 분리 | 레이어 간 의존성 분리, API 스펙 변경 영향 최소화 |
| Value Object 사용 | 불변성 보장, 도메인 개념 명시화, 타입 안전성 |
| Repository Interface | 테스트 용이성, 구현체 교체 가능 |
| Global Exception Handler | 일관된 에러 응답, 중복 제거 |

### 7.2 주의할 점

- **순환 참조 금지**: 계층 간 단방향 의존만 허용
  ```
  Controller → Service → Repository → Entity
  (역방향 금지)
  ```

- **Transaction 경계**: Service 메서드 단위로 설정
  ```java
  @Transactional  // 여기서 시작
  public void processOrder(...) {
      // DB 작업들
  }  // 여기서 커밋/롤백
  ```

- **Entity 외부 노출 금지**: 항상 DTO로 변환하여 반환
  ```java
  // 올바른 예
  return memberMapper.toResponse(member);

  // 잘못된 예
  return member;  // Entity 직접 반환 금지
  ```

---

## 8. 다음 학습 내용

- `07-error-handling-guide.md`: 에러 처리 전략
- `08-testing-guide.md`: 테스트 작성 가이드
- `09-security-checklist.md`: 보안 체크리스트
- `10-code-review-guide.md`: 코드 리뷰 가이드

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
