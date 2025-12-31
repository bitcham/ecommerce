# 13. 성능 최적화 가이드 (Performance Optimization Guide)

> **학습 목표**: JPA/QueryDSL 환경에서 발생하는 성능 이슈를 이해하고, 프로젝트에서 사용하는 최적화 패턴을 학습합니다.

---

## 1. N+1 문제 이해

### 1.1 N+1 문제란?

```
┌─────────────────────────────────────────────────────────────┐
│                      N+1 쿼리 문제                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Order 10개 조회 시:                                        │
│  ┌──────────────────────────────────────────┐               │
│  │ 1. SELECT * FROM orders           (1번) │               │
│  │ 2. SELECT * FROM order_items WHERE order_id=1  (N번)    │
│  │ 3. SELECT * FROM order_items WHERE order_id=2           │
│  │ 4. SELECT * FROM order_items WHERE order_id=3           │
│  │    ...                                                   │
│  │ 11. SELECT * FROM order_items WHERE order_id=10         │
│  └──────────────────────────────────────────┘               │
│                                                             │
│  총 1 + 10 = 11번 쿼리 실행 (성능 저하!)                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 언제 발생하는가?

**참조**: `Order.java:72-73`

```java
// Order 엔티티의 연관관계
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();
```

**발생 시나리오**:
```java
// ❌ N+1 발생
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    // items 접근 시 각각 추가 쿼리 발생
    order.getItems().forEach(item -> System.out.println(item.getProductName()));
}
```

---

## 2. N+1 해결 방법

### 2.1 Fetch Join

```java
// ✅ Fetch Join 사용
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.memberId = :memberId")
List<Order> findByMemberIdWithItems(@Param("memberId") Long memberId);
```

**결과**: 1번의 쿼리로 Order + OrderItem 모두 조회

```sql
SELECT o.*, i.*
FROM orders o
JOIN order_items i ON o.id = i.order_id
WHERE o.member_id = ?
```

### 2.2 @EntityGraph

```java
// ✅ EntityGraph 사용
@EntityGraph(attributePaths = {"items"})
@Query("SELECT o FROM Order o WHERE o.memberId = :memberId")
List<Order> findByMemberIdWithGraph(@Param("memberId") Long memberId);
```

### 2.3 @BatchSize

```java
// Entity 클래스에 설정
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
@BatchSize(size = 100)  // 100개씩 IN 쿼리로 조회
private List<OrderItem> items = new ArrayList<>();
```

**결과**: N번 → ceil(N/100)번으로 쿼리 감소

```sql
-- 개별 쿼리 대신 IN 쿼리 사용
SELECT * FROM order_items WHERE order_id IN (1, 2, 3, ..., 100)
```

### 2.4 방법 비교

| 방법 | 장점 | 단점 | 사용 시점 |
|------|------|------|----------|
| Fetch Join | 1번 쿼리 | 페이징 불가, 카테시안 곱 | 단건/소량 조회 |
| EntityGraph | 선언적, 유연함 | 복잡한 관계 어려움 | 단순 연관관계 |
| BatchSize | 페이징 가능 | 완벽한 1번은 아님 | 목록 조회, 페이징 |

---

## 3. QueryDSL 최적화

### 3.1 동적 쿼리 패턴

**참조**: `ProductQueryRepositoryImpl.java:32-48`

```java
@Override
public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
    List<Product> content = queryFactory
            .selectFrom(product)
            .where(
                    nameContains(condition.name()),       // null이면 조건 무시
                    keywordContains(condition.keyword()),
                    categoryEquals(condition.categoryId()),
                    statusEquals(condition.status()),
                    priceGoe(condition.minPrice()),
                    priceLoe(condition.maxPrice()),
                    excludeDeleted(condition.excludeDeleted())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifier(condition.sortType()))
            .fetch();
    // ...
}
```

### 3.2 BooleanExpression 패턴

**참조**: `ProductQueryRepositoryImpl.java:67-102`

```java
// null 반환 시 QueryDSL이 해당 조건 무시
private BooleanExpression nameContains(String name) {
    return name != null && !name.isBlank()
        ? product.name.containsIgnoreCase(name)
        : null;
}

private BooleanExpression priceGoe(BigDecimal minPrice) {
    return minPrice != null ? product.basePrice.goe(minPrice) : null;
}

private BooleanExpression priceLoe(BigDecimal maxPrice) {
    return maxPrice != null ? product.basePrice.loe(maxPrice) : null;
}
```

**핵심 포인트**:
- `BooleanExpression`이 `null`을 반환하면 QueryDSL이 해당 조건을 무시
- 동적 쿼리를 깔끔하게 구성 가능

### 3.3 동적 정렬

**참조**: `ProductQueryRepositoryImpl.java:104-114`

```java
private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
    if (sortType == null) {
        return product.createdAt.desc();  // 기본값
    }
    return switch (sortType) {
        case LATEST -> product.createdAt.desc();
        case PRICE_LOW -> product.basePrice.asc();
        case PRICE_HIGH -> product.basePrice.desc();
        case NAME_ASC -> product.name.asc();
    };
}
```

---

## 4. 페이징 최적화

### 4.1 Count 쿼리 최적화

**참조**: `MemberQueryRepositoryImpl.java:30-56`

```java
// ✅ 최적화된 패턴 - PageableExecutionUtils 사용
public Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable) {
    List<Member> content = queryFactory
            .selectFrom(member)
            .where(...)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery = queryFactory
            .select(member.count())
            .from(member)
            .where(...);

    // Count 쿼리를 지연 실행
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
}
```

**참조**: `OrderQueryRepositoryImpl.java:44-55`

```java
// Lambda 방식
return PageableExecutionUtils.getPage(content, pageable, () ->
        queryFactory
                .select(order.count())
                .from(order)
                .where(...)
                .fetchOne()
);
```

### 4.2 PageableExecutionUtils 동작 원리

```
┌─────────────────────────────────────────────────────────────┐
│              PageableExecutionUtils 최적화                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Count 쿼리 생략 조건:                                      │
│  ┌──────────────────────────────────────────┐               │
│  │ 1. 첫 페이지 && content < pageSize        │               │
│  │    → 전체 개수 = content.size()          │               │
│  │                                           │               │
│  │ 2. 마지막 페이지 도달                      │               │
│  │    → offset + content.size()             │               │
│  └──────────────────────────────────────────┘               │
│                                                             │
│  예: pageSize=20, content=15 조회됨                         │
│  → Count 쿼리 없이 totalElements=15 반환                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 대용량 페이징 - Offset 문제

```java
// ❌ 문제: offset이 커지면 성능 저하
// 100만 번째 데이터 조회 시 100만 개 스캔 후 20개 반환
SELECT * FROM products ORDER BY created_at DESC LIMIT 20 OFFSET 1000000;
```

**해결책 - Cursor 기반 페이징**:

```java
// ✅ Cursor (No Offset) 방식
public List<Product> searchProductsWithCursor(Long lastProductId, int size) {
    return queryFactory
            .selectFrom(product)
            .where(
                    product.id.lt(lastProductId)  // 마지막 ID보다 작은 것
            )
            .orderBy(product.id.desc())
            .limit(size)
            .fetch();
}
```

---

## 5. 인덱스 전략

### 5.1 인덱스가 필요한 컬럼

```sql
-- 검색 조건에 자주 사용되는 컬럼
CREATE INDEX idx_member_email ON members(email);
CREATE INDEX idx_member_status ON members(status);
CREATE INDEX idx_order_member_id ON orders(member_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_seller ON products(seller_id);

-- 복합 인덱스 (자주 함께 조회되는 컬럼)
CREATE INDEX idx_order_member_status ON orders(member_id, status);
CREATE INDEX idx_product_status_created ON products(status, created_at);
```

### 5.2 인덱스 설계 원칙

| 원칙 | 설명 |
|------|------|
| 카디널리티 높은 컬럼 우선 | email > status (고유값이 많은 컬럼) |
| WHERE 절 순서 | 복합 인덱스 순서 = 조회 조건 순서 |
| 커버링 인덱스 | SELECT 컬럼이 인덱스에 포함되면 테이블 접근 없이 조회 |
| 과도한 인덱스 지양 | INSERT/UPDATE 성능 저하 |

### 5.3 EXPLAIN으로 쿼리 분석

```sql
EXPLAIN SELECT * FROM members WHERE email = 'test@example.com';

-- 결과 해석
-- type: const (최적), ref (인덱스 사용), ALL (Full Scan - 비효율)
-- key: 사용된 인덱스명
-- rows: 예상 스캔 행 수
```

---

## 6. 캐싱 전략

### 6.1 Spring Cache 적용

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}
```

### 6.2 캐시 사용 예시

```java
@Service
public class ProductService {

    // 조회 시 캐시 사용
    @Cacheable(value = "product", key = "#productId")
    public ProductResponse getProduct(Long productId) {
        return productRepository.findById(productId)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 수정 시 캐시 갱신
    @CachePut(value = "product", key = "#productId")
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        // ...
    }

    // 삭제 시 캐시 제거
    @CacheEvict(value = "product", key = "#productId")
    public void deleteProduct(Long productId) {
        // ...
    }
}
```

### 6.3 캐시 적용 대상

| 대상 | 캐시 적합성 | 이유 |
|------|------------|------|
| 상품 상세 | ⭐⭐⭐ | 읽기 빈도 높음, 변경 적음 |
| 카테고리 목록 | ⭐⭐⭐ | 거의 변경 없음 |
| 회원 정보 | ⭐⭐ | 개인정보, 갱신 필요 |
| 주문 내역 | ⭐ | 변경 빈번, 정확성 중요 |
| 재고 수량 | ❌ | 실시간 정확성 필수 |

---

## 7. 트랜잭션 최적화

### 7.1 읽기 전용 트랜잭션

```java
@Service
@RequiredArgsConstructor
public class MemberService {

    // ✅ 읽기 전용 - 더티 체킹 비활성화로 성능 향상
    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 쓰기 작업
    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.update(request);
        return MemberMapper.toResponse(member);
    }
}
```

### 7.2 읽기 전용 트랜잭션 이점

```
┌─────────────────────────────────────────────────────────────┐
│              readOnly = true 이점                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 더티 체킹 비활성화                                       │
│     - 엔티티 변경 감지 스냅샷 생성 안 함                     │
│     - 메모리 절약 + flush 시간 절약                         │
│                                                             │
│  2. DB 레플리카 분산 (설정 시)                               │
│     - Master: 쓰기 전용                                     │
│     - Slave: 읽기 전용 → readOnly=true 요청 라우팅          │
│                                                             │
│  3. JDBC 드라이버 최적화                                    │
│     - 일부 드라이버에서 읽기 전용 모드 최적화               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.3 트랜잭션 범위 최소화

```java
// ❌ 나쁜 예시: 외부 API 호출이 트랜잭션 내에 있음
@Transactional
public void processOrder(OrderRequest request) {
    Order order = createOrder(request);
    orderRepository.save(order);

    // 외부 API 호출 - 응답 지연 시 트랜잭션 장시간 유지
    paymentGateway.requestPayment(order);  // ❌

    order.markAsPaid();
}

// ✅ 좋은 예시: 트랜잭션 분리
public void processOrder(OrderRequest request) {
    Order order = createAndSaveOrder(request);  // @Transactional
    PaymentResult result = paymentGateway.requestPayment(order);  // 트랜잭션 외부
    markOrderAsPaid(order.getId(), result);  // @Transactional
}
```

---

## 8. 쿼리 최적화 체크리스트

### 8.1 개발 시 확인

- [ ] N+1 쿼리 발생 여부 (로그 확인)
- [ ] 불필요한 컬럼 조회 여부 (SELECT * 지양)
- [ ] 적절한 페이징 적용
- [ ] 인덱스 활용 여부

### 8.2 코드 리뷰 시 확인

- [ ] `@Transactional(readOnly = true)` 적절히 사용
- [ ] Fetch Join / BatchSize 적용 여부
- [ ] 동적 쿼리에 BooleanExpression 패턴 사용
- [ ] PageableExecutionUtils 사용 여부

### 8.3 운영 시 모니터링

- [ ] 슬로우 쿼리 로그 확인
- [ ] 커넥션 풀 사용량
- [ ] 캐시 히트율

---

## 9. 성능 테스트

### 9.1 로컬 쿼리 로깅

```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # 바인딩 파라미터
```

### 9.2 쿼리 수 검증 테스트

```java
@DataJpaTest
class OrderQueryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 조회 시 N+1 발생하지 않음")
    void findOrderWithItems_NoNPlusOne() {
        // given
        createTestOrders(10);
        em.flush();
        em.clear();

        // when - 쿼리 수 카운트 시작
        Statistics stats = em.unwrap(Session.class).getSessionFactory().getStatistics();
        stats.clear();

        List<Order> orders = orderRepository.findAllWithItems();
        orders.forEach(o -> o.getItems().size());  // 강제 초기화

        // then
        assertThat(stats.getQueryExecutionCount()).isEqualTo(1);  // 1번만 실행
    }
}
```

---

## 10. 성능 최적화 우선순위

```
┌─────────────────────────────────────────────────────────────┐
│                 최적화 우선순위 (ROI 기준)                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1순위: N+1 해결                                            │
│         └── 가장 흔하고 영향 큼                             │
│                                                             │
│  2순위: 인덱스 추가                                         │
│         └── 적은 노력으로 큰 효과                           │
│                                                             │
│  3순위: 페이징 최적화                                       │
│         └── 대용량 데이터 처리 시 필수                      │
│                                                             │
│  4순위: 캐싱                                                │
│         └── 읽기 빈도 높은 데이터                           │
│                                                             │
│  5순위: 쿼리 튜닝                                           │
│         └── 복잡한 쿼리 개선                                │
│                                                             │
│  6순위: 아키텍처 변경                                       │
│         └── 비동기 처리, DB 분리 등                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 11. 학습 포인트

### 11.1 프로젝트에서 사용하는 패턴

| 패턴 | 적용 위치 | 효과 |
|------|----------|------|
| BooleanExpression | QueryRepositoryImpl 클래스들 | 동적 쿼리 깔끔하게 처리 |
| PageableExecutionUtils | 모든 페이징 쿼리 | 불필요한 Count 쿼리 방지 |
| readOnly 트랜잭션 | 조회 서비스 메서드 | 성능 향상 + 의도 명확화 |
| @BatchSize | 컬렉션 연관관계 | N+1 완화 |

### 11.2 흔한 실수

| 실수 | 결과 | 해결 |
|------|------|------|
| 무분별한 fetch join | 카테시안 곱, 메모리 폭발 | 필요한 경우만 사용 |
| 페이징 없는 전체 조회 | OOM 가능 | 항상 페이징 적용 |
| 트랜잭션 내 외부 호출 | 커넥션 고갈 | 트랜잭션 분리 |
| 과도한 캐싱 | 데이터 불일치 | 변경 빈도 고려 |

### 11.3 디버깅 팁

1. **쿼리 로그 활성화**: 실제 실행 쿼리 확인
2. **EXPLAIN 사용**: 인덱스 사용 여부 확인
3. **영속성 컨텍스트 클리어 후 테스트**: 캐시 영향 제거

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
