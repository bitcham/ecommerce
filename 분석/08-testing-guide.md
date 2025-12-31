# 08. 테스트 작성 가이드 (Testing Guide)

> **학습 목표**: TDD 방식으로 테스트를 작성하고, 단위 테스트와 통합 테스트의 차이를 이해합니다.

---

## 1. 테스트 전략 개요

### 1.1 테스트 피라미드

```
                    ┌─────────┐
                    │   E2E   │  ← 적게
                    ├─────────┤
                    │Integration│  ← 적당히
                    ├───────────┤
                    │   Unit    │  ← 많이
                    └───────────┘
```

| 테스트 유형 | 범위 | 속도 | 비율 |
|------------|------|------|------|
| Unit Test | 단일 클래스/메서드 | 빠름 | 70% |
| Integration Test | 여러 컴포넌트 통합 | 중간 | 20% |
| E2E Test | 전체 시스템 | 느림 | 10% |

### 1.2 TDD 사이클

```
┌───────────────────────────────────────────────────────┐
│                                                       │
│    ┌─────────┐         ┌─────────┐         ┌─────────┐│
│    │  RED    │ ──────▶ │  GREEN  │ ──────▶ │REFACTOR ││
│    │ 테스트  │         │  구현   │         │  개선   ││
│    │  작성   │         │         │         │         ││
│    └─────────┘         └─────────┘         └─────────┘│
│         ▲                                       │     │
│         └───────────────────────────────────────┘     │
│                                                       │
└───────────────────────────────────────────────────────┘
```

**프로세스**:
1. **RED**: 실패하는 테스트 먼저 작성
2. **GREEN**: 테스트를 통과하는 최소한의 코드 작성
3. **REFACTOR**: 코드 품질 개선 (테스트는 계속 통과)

---

## 2. 테스트 구조

### 2.1 테스트 클래스 구조

**참조**: `MemberTest.java:18-393`

```java
@DisplayName("Member Domain Tests")  // 클래스 설명
class MemberTest {

    // ========== 기능별 그룹 ==========

    @Nested
    @DisplayName("Member Creation")
    class MemberCreation {

        @Test
        @DisplayName("Should create member with PENDING status")
        void createMember_shouldSetDefaultStatus() {
            // given - 준비

            // when - 실행

            // then - 검증
        }
    }

    @Nested
    @DisplayName("Email Verification")
    class EmailVerification {
        // 관련 테스트들...
    }
}
```

### 2.2 Given-When-Then 패턴

모든 테스트는 **AAA (Arrange-Act-Assert)** 또는 **Given-When-Then** 패턴을 따릅니다.

**참조**: `MemberTest.java:62-72`

```java
@Test
@DisplayName("Should activate member when verifying pending member")
void verifyEmail_pendingMember_shouldActivate() {
    // given - 테스트 준비 (상태 설정)
    Member member = createPendingMember();

    // when - 테스트 대상 실행
    member.verifyEmail();

    // then - 결과 검증
    assertThat(member.isEmailVerified()).isTrue();
    assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
}
```

### 2.3 테스트 메서드 네이밍

**규칙**: `{메서드명}_{조건}_{기대결과}`

**예시**:
```java
void getMember_withValidId_shouldReturnMember()
void register_withDuplicateEmail_shouldThrowException()
void canLogin_suspendedMember_shouldReturnFalse()
void addAddress_exceedLimit_shouldThrowException()
```

---

## 3. 단위 테스트 (Unit Test)

### 3.1 도메인 테스트

**참조**: `MemberTest.java`

도메인 엔티티의 비즈니스 로직을 테스트합니다.

```java
@Nested
@DisplayName("Status Transitions")
class StatusTransitions {

    @Test
    @DisplayName("Should suspend active member")
    void suspend_activeMember_shouldSuspend() {
        // given
        Member member = createActiveMember();

        // when
        member.suspend();

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should throw exception when suspending withdrawn member")
    void suspend_withdrawnMember_shouldThrowException() {
        // given
        Member member = createWithdrawnMember();

        // when & then
        assertThatThrownBy(member::suspend)
                .isInstanceOf(InvalidStateException.class);
    }
}
```

### 3.2 서비스 테스트 (Mocking)

**참조**: `MemberServiceTest.java:40-417`

```java
@ExtendWith(MockitoExtension.class)  // Mockito 확장
@DisplayName("MemberService Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks  // Mock들을 주입받음
    private MemberServiceImpl memberService;

    @Nested
    @DisplayName("Register Member")
    class RegisterMember {

        @Test
        @DisplayName("Should register member successfully with valid request")
        void register_withValidRequest_shouldSucceed() {
            // given
            MemberCreateRequest request = MemberCreateRequest.builder()
                    .email("test@example.com")
                    .password("Password1!")
                    .passwordConfirm("Password1!")
                    .name("TestUser")
                    .phone("010-1234-5678")
                    .build();

            Member member = MemberFixture.createPendingMember();
            MemberResponse expectedResponse = MemberResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("TestUser")
                    .status(MemberStatus.PENDING)
                    .build();

            // Mock 설정 - BDD 스타일
            given(memberRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(memberRepository.save(any(Member.class))).willReturn(member);
            given(memberMapper.toResponse(any(Member.class))).willReturn(expectedResponse);

            // when
            MemberResponse result = memberService.register(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("test@example.com");

            // Mock 호출 검증
            verify(memberRepository).existsByEmail("test@example.com");
            verify(memberRepository).save(any(Member.class));
        }
    }
}
```

### 3.3 예외 테스트

```java
@Test
@DisplayName("Should throw exception when email already exists")
void register_withDuplicateEmail_shouldThrowException() {
    // given
    MemberCreateRequest request = MemberCreateRequest.builder()
            .email("existing@example.com")
            .password("Password1!")
            .passwordConfirm("Password1!")
            .name("TestUser")
            .build();

    given(memberRepository.existsByEmail("existing@example.com")).willReturn(true);

    // when & then
    assertThatThrownBy(() -> memberService.register(request))
            .isInstanceOf(DuplicateResourceException.class);
}
```

**예외 상세 검증**:
```java
assertThatThrownBy(() -> memberService.register(request))
        .isInstanceOf(DuplicateResourceException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.MEMBER_EMAIL_DUPLICATED);
```

---

## 4. Fixture 패턴

### 4.1 테스트 Fixture 클래스

**참조**: `MemberFixture.java:11-75`

테스트용 객체 생성을 중앙화합니다.

```java
public class MemberFixture {

    // 기본값 상수
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PASSWORD = "encodedPassword123";
    public static final String DEFAULT_NAME = "TestUser";
    public static final String DEFAULT_PHONE = "010-1234-5678";

    // 다양한 상태의 Member 생성
    public static Member createPendingMember() {
        return Member.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .name(DEFAULT_NAME)
                .phone(DEFAULT_PHONE)
                .build();
    }

    public static Member createActiveMember() {
        Member member = createPendingMember();
        member.verifyEmail();  // PENDING → ACTIVE
        return member;
    }

    public static Member createSuspendedMember() {
        Member member = createActiveMember();
        member.suspend();  // ACTIVE → SUSPENDED
        return member;
    }

    public static Member createWithdrawnMember() {
        Member member = createActiveMember();
        member.delete();  // ACTIVE → WITHDRAWN
        return member;
    }

    // 특수 케이스 - Reflection 사용
    public static Member createAdminMember() {
        Member member = createActiveMember();
        ReflectionTestUtils.setField(member, "role", MemberRole.ADMIN);
        return member;
    }
}
```

### 4.2 Fixture 사용

```java
import static platform.ecommerce.fixture.MemberFixture.*;

@Test
void test() {
    // Fixture 사용
    Member active = createActiveMember();
    Member suspended = createSuspendedMember();

    // 커스텀 이메일로 생성
    Member custom = createActiveMember("custom@example.com");
}
```

---

## 5. Mockito 활용

### 5.1 기본 Mock 설정

```java
// given 스타일 (BDD - 권장)
given(repository.findById(1L)).willReturn(Optional.of(member));

// when 스타일 (전통)
when(repository.findById(1L)).thenReturn(Optional.of(member));
```

### 5.2 다양한 Mock 시나리오

```java
// 빈 결과
given(repository.findById(anyLong())).willReturn(Optional.empty());

// 예외 발생
given(repository.save(any())).willThrow(new RuntimeException("DB Error"));

// 연속 호출 (첫 번째, 두 번째...)
given(repository.count())
    .willReturn(0L)      // 첫 번째 호출
    .willReturn(1L);     // 두 번째 호출

// 인자 캡처
ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
verify(repository).save(captor.capture());
Member saved = captor.getValue();
assertThat(saved.getEmail()).isEqualTo("test@example.com");
```

### 5.3 호출 검증

```java
// 호출 여부
verify(repository).save(any(Member.class));

// 호출 횟수
verify(repository, times(2)).findById(anyLong());
verify(repository, never()).delete(any());

// 호출 순서
InOrder inOrder = inOrder(repository, eventPublisher);
inOrder.verify(repository).save(any());
inOrder.verify(eventPublisher).publish(any());
```

---

## 6. AssertJ 활용

### 6.1 기본 검증

```java
import static org.assertj.core.api.Assertions.*;

// 동등성
assertThat(result).isEqualTo(expected);
assertThat(result).isNotEqualTo(other);

// null 체크
assertThat(result).isNotNull();
assertThat(result).isNull();

// boolean
assertThat(member.isActive()).isTrue();
assertThat(member.isDeleted()).isFalse();

// 문자열
assertThat(email).contains("@");
assertThat(name).startsWith("Test");
assertThat(password).hasSize(10);
```

### 6.2 컬렉션 검증

```java
// 크기
assertThat(addresses).hasSize(3);
assertThat(addresses).isEmpty();
assertThat(addresses).isNotEmpty();

// 요소 포함
assertThat(roles).contains(MemberRole.CUSTOMER);
assertThat(roles).containsExactly(MemberRole.CUSTOMER, MemberRole.SELLER);
assertThat(roles).containsExactlyInAnyOrder(MemberRole.SELLER, MemberRole.CUSTOMER);

// 조건 검증
assertThat(members)
    .allMatch(m -> m.getStatus() == MemberStatus.ACTIVE);
assertThat(orders)
    .extracting("status")
    .containsOnly(OrderStatus.COMPLETED);
```

### 6.3 예외 검증

```java
// 기본
assertThatThrownBy(() -> service.withdraw(memberId))
    .isInstanceOf(EntityNotFoundException.class);

// 메시지 검증
assertThatThrownBy(() -> service.withdraw(memberId))
    .isInstanceOf(EntityNotFoundException.class)
    .hasMessageContaining("not found");

// 특정 예외 타입
assertThatExceptionOfType(InvalidStateException.class)
    .isThrownBy(() -> order.cancel())
    .withMessageContaining("cannot cancel");

// 예외가 발생하지 않아야 할 때
assertThatCode(() -> service.process())
    .doesNotThrowAnyException();
```

---

## 7. 테스트 패턴

### 7.1 경계값 테스트

```java
@Nested
@DisplayName("Address Limit")
class AddressLimit {

    @Test
    @DisplayName("Should allow adding up to 10 addresses")
    void addAddress_atLimit_shouldSucceed() {
        Member member = createActiveMember();

        // 9개까지 추가 (한도 미만)
        for (int i = 0; i < 9; i++) {
            member.addAddress("Address" + i, ...);
        }

        // 10번째도 성공해야 함
        assertThatCode(() ->
            member.addAddress("Address10", ...)
        ).doesNotThrowAnyException();

        assertThat(member.getAddresses()).hasSize(10);
    }

    @Test
    @DisplayName("Should reject 11th address")
    void addAddress_exceedLimit_shouldThrowException() {
        Member member = createActiveMember();

        // 10개 추가 (한도)
        for (int i = 0; i < 10; i++) {
            member.addAddress("Address" + i, ...);
        }

        // 11번째는 실패해야 함
        assertThatThrownBy(() ->
            member.addAddress("Extra", ...)
        ).isInstanceOf(InvalidStateException.class);
    }
}
```

### 7.2 상태 전이 테스트

```java
@Nested
@DisplayName("Order Status Transitions")
class OrderStatusTransitions {

    @Test
    @DisplayName("Valid transitions")
    void shouldAllowValidTransitions() {
        Order order = createOrder();

        // PENDING → PAID
        order.markAsPaid();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        // PAID → PREPARING
        order.startPreparing();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("Invalid transition should throw")
    void shouldRejectInvalidTransitions() {
        Order order = createOrder();  // PENDING

        // PENDING에서 바로 SHIPPED는 불가
        assertThatThrownBy(order::ship)
            .isInstanceOf(InvalidStateException.class);
    }
}
```

### 7.3 권한 테스트

```java
@Nested
@DisplayName("Role-based Access")
class RoleBasedAccess {

    @Test
    @DisplayName("Admin can access admin endpoints")
    void admin_canAccessAdminEndpoints() {
        Member admin = createAdminMember();
        assertThat(admin.getRole()).isEqualTo(MemberRole.ADMIN);
        assertThat(admin.hasRole(MemberRole.ADMIN)).isTrue();
    }

    @Test
    @DisplayName("Customer cannot upgrade to Admin")
    void customer_cannotUpgradeToAdmin() {
        Member customer = createActiveMember();
        // Admin 승격 메서드가 없으므로 Reflection으로도 불가
        assertThat(customer.getRole()).isEqualTo(MemberRole.CUSTOMER);
    }
}
```

---

## 8. 테스트 조직

### 8.1 패키지 구조

```
src/test/java/platform/ecommerce/
├── domain/                    # 도메인 단위 테스트
│   ├── member/
│   │   └── MemberTest.java
│   ├── order/
│   │   └── OrderTest.java
│   └── product/
│       └── ProductTest.java
├── service/                   # 서비스 단위 테스트
│   ├── MemberServiceTest.java
│   ├── OrderServiceTest.java
│   └── product/
│       └── ProductServiceTest.java
├── controller/                # 컨트롤러 테스트
│   └── MemberControllerTest.java
├── repository/                # 리포지토리 통합 테스트
│   └── MemberRepositoryTest.java
├── fixture/                   # 테스트 Fixture
│   ├── MemberFixture.java
│   ├── OrderFixture.java
│   └── ProductFixture.java
└── integration/               # 통합 테스트
    └── OrderIntegrationTest.java
```

### 8.2 테스트 어노테이션

| 어노테이션 | 용도 |
|-----------|------|
| `@ExtendWith(MockitoExtension.class)` | Mockito 사용 |
| `@SpringBootTest` | 전체 컨텍스트 로드 (통합 테스트) |
| `@DataJpaTest` | JPA 관련 테스트만 |
| `@WebMvcTest` | Controller 테스트만 |
| `@Nested` | 테스트 그룹화 |
| `@DisplayName` | 테스트 설명 |
| `@Disabled` | 테스트 비활성화 (임시) |

---

## 9. 테스트 체크리스트

### 9.1 작성 시 체크리스트

- [ ] **Given-When-Then 구조** 준수
- [ ] **DisplayName** 작성 (무엇을 테스트하는지 명확하게)
- [ ] **하나의 테스트 = 하나의 검증** 원칙
- [ ] **경계값** 테스트 포함
- [ ] **예외 케이스** 테스트 포함
- [ ] **Fixture** 사용하여 중복 제거

### 9.2 리뷰 시 체크리스트

- [ ] 테스트가 실제로 실패할 수 있는가? (항상 통과하면 의미 없음)
- [ ] 테스트가 구현 세부사항이 아닌 **행위**를 테스트하는가?
- [ ] Mock이 과도하게 사용되지 않았는가?
- [ ] 테스트 데이터가 현실적인가?

---

## 10. 흔한 실수와 해결

### 10.1 과도한 Mocking

```java
// ❌ Bad - 모든 것을 Mock
@Test
void processOrder() {
    given(orderRepository.findById(any())).willReturn(Optional.of(order));
    given(productRepository.findById(any())).willReturn(Optional.of(product));
    given(paymentService.process(any())).willReturn(payment);
    given(stockService.decrease(any(), anyInt())).willReturn(true);
    given(notificationService.send(any())).willReturn(true);
    // ... 10개의 Mock

    orderService.process(orderId);

    // 테스트가 너무 복잡해짐
}

// ✅ Good - 통합 테스트로 전환하거나 단위 분리
@Test
void processOrder_shouldDecreaseStock() {
    // 재고 감소만 집중 테스트
    given(stockService.decrease(productId, quantity)).willReturn(true);

    boolean result = orderService.decreaseStock(productId, quantity);

    assertThat(result).isTrue();
    verify(stockService).decrease(productId, quantity);
}
```

### 10.2 구현 세부사항 테스트

```java
// ❌ Bad - 구현 세부사항 테스트
@Test
void register_shouldCallMethodsInOrder() {
    service.register(request);

    InOrder inOrder = inOrder(repository, encoder, mapper);
    inOrder.verify(encoder).encode(any());      // 구현 순서에 의존
    inOrder.verify(repository).save(any());
    inOrder.verify(mapper).toResponse(any());
}

// ✅ Good - 행위(결과) 테스트
@Test
void register_shouldReturnMemberWithCorrectEmail() {
    MemberResponse result = service.register(request);

    assertThat(result.email()).isEqualTo(request.getEmail());
    assertThat(result.status()).isEqualTo(MemberStatus.PENDING);
}
```

### 10.3 테스트 간 의존성

```java
// ❌ Bad - 테스트 간 상태 공유
class OrderTest {
    private static Order sharedOrder = new Order();  // static 공유

    @Test
    void test1() {
        sharedOrder.confirm();  // 상태 변경
    }

    @Test
    void test2() {
        // test1의 영향을 받음!
    }
}

// ✅ Good - 각 테스트가 독립적
class OrderTest {

    @Test
    void test1() {
        Order order = createOrder();  // 새로운 인스턴스
        order.confirm();
    }

    @Test
    void test2() {
        Order order = createOrder();  // 독립적인 인스턴스
        // test1과 무관
    }
}
```

---

## 11. 학습 포인트

### 11.1 핵심 원칙

| 원칙 | 설명 |
|------|------|
| **FIRST** | Fast, Isolated, Repeatable, Self-validating, Timely |
| **Right BICEP** | Right, Boundary, Inverse, Cross-check, Error, Performance |
| **행위 테스트** | 구현이 아닌 결과/행위를 검증 |

### 11.2 다음 학습

- `09-security-checklist.md`: 보안 테스트 포함
- `10-code-review-guide.md`: 테스트 코드 리뷰 관점

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
