# 10. 코드 리뷰 가이드 (Code Review Guide)

> **학습 목표**: 효과적인 코드 리뷰 방법을 이해하고, 리뷰어와 작성자 모두의 역할을 학습합니다.

---

## 1. 코드 리뷰의 목적

### 1.1 왜 코드 리뷰를 하는가?

```
┌─────────────────────────────────────────────────────────────┐
│                    코드 리뷰의 목적                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 품질 향상        - 버그 조기 발견                        │
│                      - 설계 개선                            │
│                                                             │
│  2. 지식 공유        - 코드베이스 이해                       │
│                      - 팀 역량 향상                         │
│                                                             │
│  3. 일관성 유지      - 코딩 컨벤션 준수                      │
│                      - 아키텍처 패턴 유지                    │
│                                                             │
│  4. 협업 문화        - 상호 학습                            │
│                      - 코드 소유권 공유                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 CLAUDE.md 워크플로우

**참조**: `CLAUDE.md`

```
[Tests Pass]
    ↓
[Senior Review] → 코드 품질, 가독성, 패턴 → Improve → Test
    ↓
[Lead Review] → 아키텍처, 확장성, 보안 → Improve → Test
    ↓
[Refactor] → 중복 제거, 명확성 개선
    ↓
[Done]
```

---

## 2. 리뷰어 가이드

### 2.1 리뷰 관점 계층

| 단계 | 관점 | 체크 포인트 |
|------|------|------------|
| 1 | **정확성** | 로직이 올바른가? |
| 2 | **보안** | 취약점이 있는가? |
| 3 | **성능** | 비효율적인 부분이 있는가? |
| 4 | **설계** | 구조가 적절한가? |
| 5 | **가독성** | 이해하기 쉬운가? |
| 6 | **스타일** | 컨벤션을 따르는가? |

### 2.2 각 계층별 상세 체크리스트

#### Level 1: 정확성

```java
// ✅ 검토 포인트

// 1. 경계값 처리
if (quantity <= 0) {  // 0 포함 여부 확인
    throw new InvalidStateException(...);
}

// 2. null 안전성
Optional<Member> member = repository.findById(id);
return member.orElseThrow(() -> ...);  // null 체크 확인

// 3. 비즈니스 로직 정확성
order.addItem(product, quantity);
// 재고 차감이 함께 이루어지는가?
// 가격 계산이 정확한가?
```

#### Level 2: 보안

```java
// ✅ 검토 포인트

// 1. 권한 검증
@PreAuthorize("hasRole('ADMIN')")  // 또는 서비스 레벨 검증
public void deleteProduct(Long productId) { ... }

// 2. 소유권 확인
if (!order.getMemberId().equals(currentMemberId)) {
    throw new BusinessException(ErrorCode.FORBIDDEN);
}

// 3. 입력값 검증
@Valid @RequestBody OrderCreateRequest request  // @Valid 존재 확인

// 4. 민감정보 로깅 방지
log.info("Order created: {}", orderId);  // 카드번호 등 노출 확인
```

#### Level 3: 성능

```java
// ❌ N+1 문제
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    order.getItems().size();  // 각 주문마다 추가 쿼리
}

// ✅ Fetch Join 사용
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.member.id = :memberId")
List<Order> findByMemberIdWithItems(@Param("memberId") Long memberId);
```

```java
// ❌ 불필요한 데이터 로드
List<Product> products = productRepository.findAll();  // 전체 로드

// ✅ 페이징 사용
Page<Product> products = productRepository.findAll(pageable);
```

#### Level 4: 설계

```java
// ❌ 책임 분리 미흡
@Service
public class OrderService {
    public void createOrder(...) {
        // 주문 생성
        // 결제 처리
        // 이메일 발송
        // 재고 감소
        // ... 하나의 메서드에 모든 것
    }
}

// ✅ 책임 분리
@Service
public class OrderService {
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final StockService stockService;

    public void createOrder(...) {
        Order order = Order.create(...);
        orderRepository.save(order);

        paymentService.process(order);
        stockService.decrease(order.getItems());
        notificationService.sendOrderConfirmation(order);
    }
}
```

#### Level 5: 가독성

```java
// ❌ 이해하기 어려운 코드
public boolean check(Member m, Order o) {
    return m.getStatus() == MemberStatus.ACTIVE
        && !m.isDeleted()
        && o.getStatus() == OrderStatus.PENDING
        && o.getMemberId().equals(m.getId())
        && o.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30));
}

// ✅ 의미 있는 이름과 분리
public boolean canProcessOrder(Member member, Order order) {
    return member.isActive()
        && order.isPending()
        && order.isOwnedBy(member)
        && order.isWithinProcessingWindow();
}
```

#### Level 6: 스타일

```java
// 프로젝트 컨벤션 준수 확인
// - 네이밍 규칙
// - 들여쓰기
// - import 정리
// - Javadoc 형식
```

---

## 3. 리뷰 코멘트 작성법

### 3.1 좋은 코멘트 vs 나쁜 코멘트

| ❌ 나쁜 코멘트 | ✅ 좋은 코멘트 |
|---------------|---------------|
| "이건 잘못됐어요" | "이 부분에서 null이 들어오면 NPE가 발생할 수 있어요. Optional 사용을 고려해주세요." |
| "왜 이렇게 했어요?" | "이 접근법을 선택한 이유가 궁금해요. X 방식도 고려해보셨나요?" |
| "수정하세요" | "성능 개선을 위해 Fetch Join 사용을 제안드려요. 현재 N+1 쿼리가 발생하고 있어요." |

### 3.2 코멘트 접두사

| 접두사 | 의미 | 예시 |
|--------|------|------|
| `[MUST]` | 반드시 수정 필요 | 보안 취약점, 버그 |
| `[SHOULD]` | 강력 권장 | 성능 이슈, 설계 개선 |
| `[COULD]` | 제안 | 가독성, 스타일 |
| `[QUESTION]` | 질문 | 구현 의도 확인 |
| `[NITPICK]` | 사소한 것 | 오타, 포맷팅 |

### 3.3 예시

```markdown
[MUST] 보안 이슈
이 엔드포인트에서 소유권 검증이 없어요. 다른 사용자의 주문도 조회할 수 있어요.

```java
// 추가 필요
if (!order.getMemberId().equals(currentMemberId)) {
    throw new BusinessException(ErrorCode.FORBIDDEN);
}
```
```

```markdown
[SHOULD] 성능 개선
현재 코드에서 N+1 쿼리가 발생해요. 10개 주문 조회 시 11번의 쿼리가 실행돼요.
Fetch Join 사용을 권장드려요.
```

```markdown
[COULD] 가독성 개선
이 조건문이 복잡해서 별도 메서드로 추출하면 어떨까요?
`isEligibleForDiscount()` 같은 이름으로요.
```

```markdown
[QUESTION]
여기서 `synchronized` 대신 `@Transactional`로 동시성을 처리한 이유가 있을까요?
```

---

## 4. 작성자 가이드

### 4.1 PR(Pull Request) 작성 전

- [ ] 자체 코드 리뷰 완료
- [ ] 테스트 통과 확인
- [ ] 불필요한 주석/디버그 코드 제거
- [ ] 커밋 메시지 정리

### 4.2 PR 설명 템플릿

```markdown
## 요약
무엇을 왜 변경했는지 간단히 설명

## 변경 사항
- [ ] 기능 A 추가
- [ ] 버그 B 수정
- [ ] 성능 C 개선

## 테스트
- [ ] 단위 테스트 추가
- [ ] 통합 테스트 통과
- [ ] 수동 테스트 완료

## 리뷰 포인트
특별히 리뷰가 필요한 부분이 있다면 명시

## 관련 이슈
#123
```

### 4.3 리뷰 피드백 대응

```markdown
# 좋은 대응

리뷰어: "[SHOULD] 이 부분은 N+1 쿼리가 발생해요"

작성자: "감사합니다! Fetch Join으로 수정했어요.
추가로 테스트에서 쿼리 수를 검증하는 코드도 넣었어요."

# 토론이 필요한 경우

작성자: "좋은 지적이에요. 다만 이 경우는 데이터가 최대 5개라서
성능 영향이 미미할 것 같아요. 그래도 수정할까요?"
```

---

## 5. 도메인별 리뷰 포인트

### 5.1 Controller 리뷰

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    // ✅ 체크 포인트
    // 1. @Valid 어노테이션
    // 2. 적절한 HTTP 메서드
    // 3. 응답 형식 일관성
    // 4. 에러 처리 위임

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createOrder(memberId, request)));
    }
}
```

### 5.2 Service 리뷰

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // ✅ 체크 포인트
    // 1. 트랜잭션 설정
    // 2. 예외 처리
    // 3. 비즈니스 로직 정확성
    // 4. 의존성 주입

    @Override
    @Transactional  // 쓰기 작업
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 비즈니스 검증
        validateOrderCreation(member, request);

        Order order = Order.create(member, request.getItems());
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }
}
```

### 5.3 Entity 리뷰

```java
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    // ✅ 체크 포인트
    // 1. 불변식(Invariant) 보장
    // 2. 연관관계 설정
    // 3. 비즈니스 메서드 캡슐화
    // 4. equals/hashCode (필요시)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    // 팩토리 메서드
    public static Order create(Member member, List<OrderItemRequest> items) {
        Order order = new Order();
        order.member = member;
        order.status = OrderStatus.PENDING;

        items.forEach(item -> order.addItem(item));
        return order;
    }

    // 비즈니스 메서드
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new InvalidStateException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

### 5.4 Test 리뷰

```java
@DisplayName("Order Domain Tests")
class OrderTest {

    // ✅ 체크 포인트
    // 1. Given-When-Then 구조
    // 2. 경계값 테스트
    // 3. 예외 케이스
    // 4. 테스트 격리

    @Test
    @DisplayName("Should cancel pending order")
    void cancel_pendingOrder_shouldSucceed() {
        // given
        Order order = OrderFixture.createPendingOrder();

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw when cancelling shipped order")
    void cancel_shippedOrder_shouldThrowException() {
        // given
        Order order = OrderFixture.createShippedOrder();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidStateException.class);
    }
}
```

---

## 6. 리뷰 체크리스트 (Quick Reference)

### 6.1 필수 체크 (MUST)

- [ ] **보안**: 인증/인가, 소유권 검증, 입력값 검증
- [ ] **정확성**: 비즈니스 로직, null 처리, 경계값
- [ ] **테스트**: 주요 기능, 예외 케이스 커버

### 6.2 권장 체크 (SHOULD)

- [ ] **성능**: N+1 쿼리, 불필요한 데이터 로드
- [ ] **설계**: 책임 분리, 적절한 추상화
- [ ] **에러 처리**: 적절한 예외 사용, 메시지

### 6.3 선택 체크 (COULD)

- [ ] **가독성**: 네이밍, 메서드 길이, 복잡도
- [ ] **스타일**: 코딩 컨벤션, 포맷팅
- [ ] **문서화**: 필요한 주석, Javadoc

---

## 7. 리뷰 프로세스

### 7.1 리뷰 사이클

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│   작성   │ ──▶ │   리뷰   │ ──▶ │   수정   │
└──────────┘     └──────────┘     └──────────┘
                      │                 │
                      │                 ▼
                      │           ┌──────────┐
                      └───────────│  재리뷰  │
                                  └──────────┘
                                       │
                                       ▼
                                 ┌──────────┐
                                 │   머지   │
                                 └──────────┘
```

### 7.2 시간 가이드라인

| 단계 | 시간 |
|------|------|
| 리뷰 시작 | PR 제출 후 24시간 이내 |
| 리뷰 완료 | 시작 후 48시간 이내 |
| 수정 응답 | 피드백 후 24시간 이내 |

### 7.3 승인 기준

- [ ] 모든 `[MUST]` 코멘트 해결
- [ ] 대부분의 `[SHOULD]` 코멘트 해결 또는 합의
- [ ] 테스트 통과
- [ ] 최소 1명의 승인

---

## 8. 학습 포인트

### 8.1 리뷰어로서 성장하기

| 단계 | 포커스 |
|------|--------|
| 초급 | 버그, 스타일, 기본 패턴 |
| 중급 | 성능, 설계, 테스트 품질 |
| 고급 | 아키텍처, 확장성, 트레이드오프 |

### 8.2 좋은 리뷰 문화

- **존중**: 코드를 비판하되 사람을 비판하지 않음
- **건설적**: 문제 지적과 함께 대안 제시
- **학습**: 리뷰도 배움의 기회
- **감사**: 좋은 코드에 대한 칭찬도 중요

---

## 9. 참고: 자주 발견되는 이슈

### 9.1 Top 10 리뷰 지적 사항

| 순위 | 이슈 | 빈도 |
|------|------|------|
| 1 | N+1 쿼리 | ★★★★★ |
| 2 | 권한 검증 누락 | ★★★★☆ |
| 3 | 예외 처리 미흡 | ★★★★☆ |
| 4 | 테스트 부족 | ★★★☆☆ |
| 5 | 입력값 검증 누락 | ★★★☆☆ |
| 6 | 트랜잭션 설정 오류 | ★★★☆☆ |
| 7 | 네이밍 불명확 | ★★☆☆☆ |
| 8 | 코드 중복 | ★★☆☆☆ |
| 9 | 주석 부족/과다 | ★☆☆☆☆ |
| 10 | 불필요한 로깅 | ★☆☆☆☆ |

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
