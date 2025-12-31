# 15. JPA 엔티티 설계 가이드 (JPA Entity Design Guide)

> **학습 목표**: 프로젝트의 JPA 엔티티 설계 패턴을 이해하고, 일관된 방식으로 엔티티를 작성할 수 있습니다.

---

## 1. 엔티티 계층 구조

### 1.1 상속 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    엔티티 상속 구조                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  BaseEntity (@MappedSuperclass)                            │
│  ├── id (Long, auto-generated)                             │
│  ├── createdAt (자동 설정)                                  │
│  └── updatedAt (자동 설정)                                  │
│       │                                                     │
│       ├── Member (implements SoftDeletable)                │
│       ├── Product (implements SoftDeletable)               │
│       ├── Order                                            │
│       └── ...                                               │
│                                                             │
│  BaseTimeEntity (@MappedSuperclass)                        │
│  ├── createdAt                                             │
│  └── updatedAt                                             │
│       │                                                     │
│       └── (ID가 별도 관리되는 엔티티용)                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 BaseEntity

**참조**: `BaseEntity.java:16-45`

```java
@Getter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### 1.3 BaseTimeEntity

**참조**: `BaseTimeEntity.java:17-29`

```java
// ID 없이 시간 필드만 필요한 경우
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## 2. Soft Delete 패턴

### 2.1 SoftDeletable 인터페이스

**참조**: `SoftDeletable.java:1-19`

```java
public interface SoftDeletable {

    LocalDateTime getDeletedAt();

    void delete();

    void restore();

    default boolean isDeleted() {
        return getDeletedAt() != null;
    }
}
```

### 2.2 구현 예시

**참조**: `Member.java:261-276`

```java
@Entity
public class Member extends BaseEntity implements SoftDeletable {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Override
    public void delete() {
        this.status = MemberStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public void restore() {
        if (this.status != MemberStatus.WITHDRAWN) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Member is not withdrawn");
        }
        this.status = MemberStatus.PENDING;
        this.deletedAt = null;
    }
}
```

### 2.3 Soft Delete vs Hard Delete

| 방식 | 설명 | 사용 시점 |
|------|------|----------|
| Soft Delete | `deletedAt` 설정, 데이터 유지 | 회원, 상품 (복구 가능) |
| Hard Delete | 실제 DB에서 삭제 | 장바구니 아이템, 임시 데이터 |

---

## 3. Value Object (VO) 패턴

### 3.1 Value Object란?

```
┌─────────────────────────────────────────────────────────────┐
│                    Value Object 특징                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 불변 (Immutable)    - 생성 후 변경 불가                  │
│  2. 동등성 (Equality)   - 값으로 비교 (id가 아님)            │
│  3. 자체 검증           - 생성 시 유효성 검증                 │
│  4. 의미 있는 연산      - 도메인 로직 캡슐화                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Money Value Object

**참조**: `Money.java:17-98`

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    // 팩토리 메서드
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return new Money(amount);
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    // 산술 연산
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    // 비교 연산
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
```

### 3.3 Email Value Object

**참조**: `Email.java:16-68`

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @Column(name = "email", length = 255)
    private String value;

    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }

    public static Email of(String email) {
        validate(email);
        return new Email(email);
    }

    private static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    // 도메인 로직
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    public String masked() {
        String local = getLocalPart();
        return local.substring(0, 2) + "***@" + getDomain();
    }
}
```

### 3.4 프로젝트 VO 목록

| VO | 용도 | 위치 |
|----|------|------|
| `Money` | 금액 표현, 산술 연산 | `domain/common/vo/Money.java` |
| `Email` | 이메일 검증, 마스킹 | `domain/common/vo/Email.java` |
| `PhoneNumber` | 전화번호 검증, 포맷팅 | `domain/common/vo/PhoneNumber.java` |
| `Address` | 주소 정보 | `domain/common/vo/Address.java` |

---

## 4. 엔티티 작성 규칙

### 4.1 기본 구조

**참조**: `Member.java:18-74`

```java
@Entity
@Table(name = "member")            // 테이블명 명시
@Getter                            // Getter만 (Setter 없음!)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용 기본 생성자
public class Member extends BaseEntity implements SoftDeletable {

    // 상수
    private static final int MAX_ADDRESS_COUNT = 10;

    // 필드
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)   // Enum은 STRING으로
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    // 연관관계
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAddress> addresses = new ArrayList<>();

    // 생성자 (Builder)
    @Builder
    public Member(String email, String password, String name, String phone) {
        validateRequired(email, "email");
        validateRequired(password, "password");
        validateRequired(name, "name");

        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = MemberRole.CUSTOMER;
        this.status = MemberStatus.PENDING;
    }

    // 비즈니스 메서드
    public void verifyEmail() { ... }
    public void updateProfile(String name, String phone, String profileImage) { ... }
}
```

### 4.2 핵심 규칙

| 규칙 | 이유 |
|------|------|
| `@Setter` 사용 금지 | 무분별한 변경 방지, 캡슐화 |
| `protected` 기본 생성자 | JPA 프록시 생성용 |
| `@Builder`로 생성 | 명시적 생성, 검증 가능 |
| 비즈니스 메서드 사용 | 도메인 로직 캡슐화 |
| `EnumType.STRING` | DB 가독성, 안전성 |

---

## 5. 연관관계 매핑

### 5.1 @OneToMany (양방향)

**참조**: `Member.java:58-59`

```java
// 부모 엔티티 (Member)
@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
private List<MemberAddress> addresses = new ArrayList<>();

// 자식 엔티티 (MemberAddress)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id", nullable = false)
private Member member;
```

### 5.2 연관관계 설정 옵션

| 옵션 | 설명 | 사용 시점 |
|------|------|----------|
| `mappedBy` | 연관관계 주인 지정 | 양방향 관계 |
| `cascade = ALL` | 부모 저장 시 자식도 저장 | 부모-자식 생명주기 동일 |
| `orphanRemoval = true` | 컬렉션에서 제거 시 DB 삭제 | 부모 없으면 의미 없는 자식 |
| `fetch = LAZY` | 지연 로딩 (N+1 주의) | 기본값, 거의 항상 사용 |

### 5.3 연관관계 편의 메서드

**참조**: `Member.java:176-203`

```java
// ✅ 좋은 예시 - 편의 메서드 사용
public MemberAddress addAddress(String addressName, ...) {
    if (this.addresses.size() >= MAX_ADDRESS_COUNT) {
        throw new InvalidStateException(ErrorCode.MEMBER_ADDRESS_LIMIT_EXCEEDED);
    }

    MemberAddress memberAddress = MemberAddress.builder()
            .member(this)  // 양방향 관계 설정
            .name(addressName)
            .build();

    this.addresses.add(memberAddress);  // 컬렉션에 추가
    return memberAddress;
}

public void removeAddress(Long addressId) {
    MemberAddress address = findAddressById(addressId);
    this.addresses.remove(address);  // orphanRemoval로 자동 삭제
}
```

---

## 6. Enum 사용 패턴

### 6.1 상태 Enum

```java
public enum MemberStatus {
    PENDING,    // 가입 대기
    ACTIVE,     // 활성
    SUSPENDED,  // 정지
    WITHDRAWN;  // 탈퇴

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
```

### 6.2 Enum에 로직 포함

```java
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    // 상태 전이 가능 여부
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
            case PAID -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == SHIPPED;
            case SHIPPED -> next == DELIVERED;
            default -> false;
        };
    }

    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID;
    }
}
```

---

## 7. 컬럼 매핑

### 7.1 @Column 속성

```java
@Column(
    name = "column_name",      // DB 컬럼명 (snake_case)
    nullable = false,          // NOT NULL 제약조건
    unique = true,             // UNIQUE 제약조건
    length = 100,              // VARCHAR 길이
    updatable = false,         // UPDATE 시 변경 불가
    columnDefinition = "TEXT"  // DDL 직접 지정
)
```

### 7.2 타입별 매핑

| Java 타입 | DB 타입 | 어노테이션 |
|----------|--------|-----------|
| `String` | VARCHAR | `@Column(length = 100)` |
| `String` | TEXT | `@Column(columnDefinition = "TEXT")` |
| `BigDecimal` | DECIMAL | `@Column(precision = 12, scale = 2)` |
| `LocalDateTime` | TIMESTAMP | 자동 |
| `Enum` | VARCHAR | `@Enumerated(EnumType.STRING)` |
| `boolean` | BOOLEAN | 자동 |

### 7.3 @Embedded

```java
// 엔티티
@Embedded
private ShippingAddress shippingAddress;

// Embeddable
@Embeddable
public class ShippingAddress {
    @Column(name = "shipping_recipient_name")
    private String recipientName;

    @Column(name = "shipping_address")
    private String address;
}
```

---

## 8. 검증 패턴

### 8.1 생성자 검증

**참조**: `Member.java:62-80`

```java
@Builder
public Member(String email, String password, String name, String phone) {
    // 필수 값 검증
    validateRequired(email, "email");
    validateRequired(password, "password");
    validateRequired(name, "name");

    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
    // 기본값 설정
    this.role = MemberRole.CUSTOMER;
    this.status = MemberStatus.PENDING;
}

private static void validateRequired(String value, String fieldName) {
    if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(fieldName + " is required");
    }
}
```

### 8.2 상태 전이 검증

**참조**: `Member.java:88-94`

```java
public void verifyEmail() {
    if (this.emailVerified) {
        throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Email is already verified");
    }
    this.emailVerified = true;
    this.status = MemberStatus.ACTIVE;
}
```

---

## 9. 체크리스트

### 9.1 새 엔티티 작성 시

- [ ] `BaseEntity` 상속
- [ ] `@Entity`, `@Table` 추가
- [ ] `@Getter` 추가 (Setter 없음!)
- [ ] `protected` 기본 생성자
- [ ] `@Builder` 생성자 + 검증
- [ ] 필수 필드에 `nullable = false`
- [ ] Enum은 `@Enumerated(EnumType.STRING)`

### 9.2 연관관계 설정 시

- [ ] `@ManyToOne`에 `fetch = LAZY`
- [ ] 양방향 관계면 편의 메서드 작성
- [ ] 생명주기 동일하면 `cascade`, `orphanRemoval`

### 9.3 비즈니스 로직

- [ ] 상태 변경은 메서드로
- [ ] 상태 전이 시 검증
- [ ] Soft Delete 필요시 `SoftDeletable` 구현

---

## 10. 학습 포인트

### 10.1 왜 Setter를 사용하지 않는가?

```java
// ❌ 나쁜 예시 - Setter 사용
member.setStatus(MemberStatus.WITHDRAWN);
member.setDeletedAt(LocalDateTime.now());

// ✅ 좋은 예시 - 의미있는 메서드
member.delete();  // 상태 변경 + deletedAt 설정 + 검증
```

**이유:**
1. 캡슐화: 내부 상태 변경 로직 숨김
2. 일관성: 관련 필드들이 함께 변경됨
3. 검증: 잘못된 상태 전이 방지

### 10.2 Value Object 사용 장점

```java
// ❌ Primitive 사용
BigDecimal price = new BigDecimal("10000");
BigDecimal discountedPrice = price.multiply(new BigDecimal("0.9"));

// ✅ Value Object 사용
Money price = Money.of(10000);
Money discountedPrice = price.percentage(new BigDecimal("90"));
```

**장점:**
1. 의미 명확: `Money`가 `BigDecimal`보다 명확
2. 검증 내장: 생성 시 자동 검증
3. 연산 캡슐화: `add()`, `subtract()` 등 제공

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
