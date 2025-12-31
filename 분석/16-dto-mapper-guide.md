# 16. DTO & Mapper 패턴 가이드 (DTO & Mapper Pattern Guide)

> **학습 목표**: 프로젝트의 DTO 설계 규칙과 MapStruct Mapper 사용법을 이해합니다.

---

## 1. DTO 개요

### 1.1 DTO란?

```
┌─────────────────────────────────────────────────────────────┐
│                    DTO (Data Transfer Object)               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  목적: 계층 간 데이터 전달                                   │
│                                                             │
│  Controller ←→ Service ←→ Repository                        │
│      ↑                                                      │
│      │                                                      │
│  Request DTO ←→ Entity ←→ Response DTO                     │
│      │                                                      │
│      ↓                                                      │
│   Client                                                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 왜 DTO를 사용하는가?

| 이유 | 설명 |
|------|------|
| **엔티티 보호** | 민감정보 (password 등) 노출 방지 |
| **API 안정성** | 엔티티 변경이 API에 영향 없음 |
| **검증 분리** | API 검증과 도메인 검증 분리 |
| **유연성** | 용도별 다른 필드 조합 가능 |

---

## 2. DTO 분류

### 2.1 패키지 구조

```
dto/
├── request/                    # 요청 DTO
│   ├── MemberCreateRequest.java
│   ├── MemberUpdateRequest.java
│   ├── MemberSearchCondition.java
│   ├── order/
│   │   ├── OrderCreateRequest.java
│   │   └── OrderSearchCondition.java
│   └── product/
│       └── ProductCreateRequest.java
│
└── response/                   # 응답 DTO
    ├── ApiResponse.java        # 표준 응답 래퍼
    ├── PageResponse.java       # 페이징 응답
    ├── ErrorResponse.java      # 에러 응답
    ├── MemberResponse.java
    ├── MemberDetailResponse.java
    └── order/
        └── OrderResponse.java
```

### 2.2 DTO 유형

| 유형 | 네이밍 | 용도 |
|------|--------|------|
| 생성 요청 | `{Entity}CreateRequest` | POST 요청 본문 |
| 수정 요청 | `{Entity}UpdateRequest` | PATCH/PUT 요청 본문 |
| 검색 조건 | `{Entity}SearchCondition` | GET 쿼리 파라미터 |
| 기본 응답 | `{Entity}Response` | 목록/단건 응답 |
| 상세 응답 | `{Entity}DetailResponse` | 연관 데이터 포함 응답 |

---

## 3. Request DTO 작성

### 3.1 생성 요청 (CreateRequest)

**참조**: `MemberCreateRequest.java:1-40`

```java
@Builder
public record MemberCreateRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$",
                message = "Password must contain uppercase, lowercase, number, and special character"
        )
        String password,

        @NotBlank(message = "Password confirmation is required")
        String passwordConfirm,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "Invalid phone number format")
        String phone
) {
    // 비즈니스 검증 메서드
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}
```

### 3.2 수정 요청 (UpdateRequest)

**참조**: `MemberUpdateRequest.java:1-22`

```java
// PATCH용 - 모든 필드 nullable (null이면 수정 안 함)
@Builder
public record MemberUpdateRequest(

        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,      // null 허용

        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")
        String phone,     // null 허용

        @Size(max = 500)
        String profileImage
) {
}
```

### 3.3 검색 조건 (SearchCondition)

**참조**: `MemberSearchCondition.java:1-28`

```java
@Builder
public record MemberSearchCondition(
        String email,           // 이메일 검색
        String name,            // 이름 검색
        MemberStatus status,    // 상태 필터
        MemberRole role,        // 역할 필터
        Boolean excludeWithdrawn  // 탈퇴 회원 제외
) {
    public MemberSearchCondition {
        // 기본값 설정
        if (excludeWithdrawn == null) {
            excludeWithdrawn = true;
        }
    }

    public static MemberSearchCondition empty() {
        return MemberSearchCondition.builder().build();
    }
}
```

---

## 4. Response DTO 작성

### 4.1 기본 응답

**참조**: `MemberResponse.java:1-25`

```java
@Builder
public record MemberResponse(
        Long id,
        String email,
        String name,
        String phone,
        String profileImage,
        MemberRole role,
        MemberStatus status,
        boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
```

### 4.2 상세 응답 (연관 데이터 포함)

**참조**: `MemberDetailResponse.java:1-28`

```java
@Builder
public record MemberDetailResponse(
        Long id,
        String email,
        String name,
        String phone,
        String profileImage,
        MemberRole role,
        MemberStatus status,
        boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        List<AddressResponse> addresses,      // 연관 데이터
        AddressResponse defaultAddress        // 계산된 필드
) {
}
```

### 4.3 중첩 Response

**참조**: `AddressResponse.java:1-20`

```java
@Builder
public record AddressResponse(
        Long id,
        String name,
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address,
        String addressDetail,
        String fullAddress,      // 계산된 필드 (address + addressDetail)
        boolean isDefault
) {
}
```

---

## 5. Validation 어노테이션

### 5.1 자주 사용하는 어노테이션

| 어노테이션 | 용도 | 예시 |
|-----------|------|------|
| `@NotBlank` | null, 빈 문자열, 공백 불가 | 필수 문자열 |
| `@NotNull` | null 불가 | 필수 객체/숫자 |
| `@Size` | 문자열/컬렉션 크기 | `@Size(min=2, max=100)` |
| `@Min`, `@Max` | 숫자 범위 | `@Min(1) @Max(99)` |
| `@Email` | 이메일 형식 | 이메일 필드 |
| `@Pattern` | 정규식 | 전화번호, 비밀번호 |
| `@Positive` | 양수 | 가격, 수량 |

### 5.2 메시지 국제화

```java
// 직접 메시지 지정
@NotBlank(message = "Email is required")

// 메시지 코드 사용
@NotBlank(message = "{member.email.required}")
```

---

## 6. MapStruct Mapper

### 6.1 MapStruct란?

```
┌─────────────────────────────────────────────────────────────┐
│                    MapStruct 장점                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 컴파일 타임 코드 생성 - 런타임 오버헤드 없음             │
│  2. 타입 안전 - 필드 매핑 오류 컴파일 시 발견               │
│  3. 자동 매핑 - 같은 이름/타입 필드 자동 매핑               │
│  4. 커스텀 매핑 - @Mapping으로 복잡한 변환 지원             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 Mapper 인터페이스

**참조**: `MemberMapper.java:17-41`

```java
@Mapper(componentModel = "spring")  // Spring Bean으로 등록
public interface MemberMapper {

    // 기본 매핑 - 같은 이름 필드 자동 매핑
    @Mapping(target = "emailVerified", source = "emailVerified")
    MemberResponse toResponse(Member member);

    // 리스트 매핑
    List<MemberResponse> toResponseList(List<Member> members);

    // 복잡한 매핑 - 연관 데이터 포함
    @Mapping(target = "addresses", source = "addresses")
    @Mapping(target = "defaultAddress", source = "member", qualifiedByName = "mapDefaultAddress")
    @Mapping(target = "emailVerified", source = "emailVerified")
    MemberDetailResponse toDetailResponse(Member member);

    // 중첩 엔티티 매핑
    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "isDefault", source = "default")  // boolean getter: isDefault()
    AddressResponse toAddressResponse(MemberAddress address);

    // 커스텀 매핑 메서드
    @Named("mapDefaultAddress")
    default AddressResponse mapDefaultAddress(Member member) {
        MemberAddress defaultAddress = member.getDefaultAddress();
        return defaultAddress != null ? toAddressResponse(defaultAddress) : null;
    }
}
```

### 6.3 @Mapping 속성

| 속성 | 설명 | 예시 |
|------|------|------|
| `target` | 대상 필드명 | `target = "emailVerified"` |
| `source` | 소스 필드명 | `source = "emailVerified"` |
| `expression` | Java 표현식 | `expression = "java(entity.getFullName())"` |
| `qualifiedByName` | 커스텀 메서드 참조 | `qualifiedByName = "mapDefaultAddress"` |
| `ignore` | 매핑 무시 | `ignore = true` |
| `constant` | 상수값 | `constant = "UNKNOWN"` |

---

## 7. Mapper 사용 패턴

### 7.1 Service에서 사용

```java
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;  // 주입

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return memberMapper.toResponse(member);  // Entity → DTO 변환
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return memberMapper.toDetailResponse(member);  // 상세 DTO 변환
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> memberPage = memberQueryRepository.searchMembers(condition, pageable);

        List<MemberResponse> content = memberMapper.toResponseList(memberPage.getContent());
        return PageResponse.of(content, memberPage);
    }
}
```

### 7.2 페이징 응답 변환

```java
// PageResponse 유틸리티
public class PageResponse<T> {
    private List<T> content;
    private PageInfo page;

    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .page(PageInfo.from(page))
                .build();
    }
}
```

---

## 8. DTO 설계 규칙

### 8.1 필드 규칙

| 규칙 | 설명 |
|------|------|
| 민감정보 제외 | password, 내부 ID 등 |
| 필요한 필드만 | 불필요한 필드 노출 금지 |
| 계산된 필드 | `fullAddress` 등 편의 필드 추가 가능 |
| Enum은 그대로 | 문자열 변환 불필요 (Jackson 자동 처리) |

### 8.2 record vs class

```java
// ✅ record 사용 (권장)
public record MemberResponse(
        Long id,
        String email,
        String name
) {}

// 필요시 class 사용
@Getter
@Builder
public class ComplexResponse {
    private final Long id;
    private final String email;
    // 복잡한 로직 필요시
}
```

**record 장점:**
- 불변(Immutable) 보장
- `equals()`, `hashCode()`, `toString()` 자동 생성
- 간결한 문법

### 8.3 네이밍 컨벤션

```
┌─────────────────────────────────────────────────────────────┐
│                    DTO 네이밍 컨벤션                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Request DTO:                                               │
│  ├── {Entity}CreateRequest    # 생성                        │
│  ├── {Entity}UpdateRequest    # 수정                        │
│  └── {Entity}SearchCondition  # 검색 조건                   │
│                                                             │
│  Response DTO:                                              │
│  ├── {Entity}Response         # 기본 응답                   │
│  ├── {Entity}DetailResponse   # 상세 응답                   │
│  └── {Entity}SummaryResponse  # 요약 응답 (목록용)          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 9. 체크리스트

### 9.1 Request DTO 작성 시

- [ ] 필수 필드에 `@NotBlank` / `@NotNull`
- [ ] 크기 제한에 `@Size` / `@Min` / `@Max`
- [ ] 형식 검증에 `@Pattern` / `@Email`
- [ ] 오류 메시지 명시
- [ ] `@Builder` 추가

### 9.2 Response DTO 작성 시

- [ ] 민감정보 제외 (password 등)
- [ ] 필요한 필드만 포함
- [ ] `@Builder` 추가
- [ ] 계산된 필드 필요시 Mapper에서 처리

### 9.3 Mapper 작성 시

- [ ] `@Mapper(componentModel = "spring")`
- [ ] 필드명 다르면 `@Mapping` 명시
- [ ] 복잡한 변환은 `@Named` 메서드
- [ ] 리스트 변환 메서드 추가

---

## 10. 학습 포인트

### 10.1 엔티티를 직접 반환하면 안 되는 이유

```java
// ❌ 나쁜 예시 - Entity 직접 반환
@GetMapping("/{id}")
public Member getMember(@PathVariable Long id) {
    return memberRepository.findById(id).orElseThrow();
}

// 문제점:
// 1. password 필드 노출
// 2. 엔티티 변경 시 API 깨짐
// 3. 순환 참조 (addresses → member → addresses...)
```

```java
// ✅ 좋은 예시 - DTO 반환
@GetMapping("/{id}")
public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
    return ApiResponse.success(memberService.getMember(id));
}
```

### 10.2 CreateRequest vs UpdateRequest 분리 이유

```java
// CreateRequest: 모든 필수 필드에 @NotBlank
@NotBlank String email;
@NotBlank String password;
@NotBlank String name;

// UpdateRequest: 모든 필드 nullable (null이면 수정 안 함)
String name;      // 없으면 유지
String phone;     // 없으면 유지
```

**이유:**
- 생성: 모든 필수 필드 필요
- 수정: 변경할 필드만 전송 (PATCH 시맨틱)

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
