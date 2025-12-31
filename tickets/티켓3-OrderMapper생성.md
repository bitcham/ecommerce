# 티켓 3: OrderMapper 생성 (MapStruct)

**Priority**: P3 (Low)
**Estimate**: 1-2시간
**Type**: Refactoring / Code Quality
**담당**: 주니어 개발자

---

## 배경

Lead Engineer 리뷰에서 **일관성 문제**가 지적되었습니다:

> "OrderServiceImpl uses manual `toResponse()` methods (lines 232-278),
> while MemberService uses MapStruct. This inconsistency makes maintenance harder."
> — Lead Engineer Review

현재 프로젝트에서 DTO 변환 방식이 혼재되어 있습니다:
- ✅ `MemberMapper` - MapStruct 사용
- ❌ `OrderServiceImpl` - 수동 변환 메서드

---

## 목표

`OrderServiceImpl`의 수동 변환 코드를 **MapStruct**로 교체하여:
1. 코드 일관성 확보
2. 보일러플레이트 코드 제거
3. 컴파일 타임 타입 안전성 확보

---

## 현재 문제 코드

**파일**: `OrderServiceImpl.java:232-278`

```java
// 현재 코드 (수동 변환 - 제거 대상)
private OrderResponse toResponse(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getStatus().name(),
        order.getTotalAmount(),
        // ... 많은 필드들
    );
}

private OrderItemResponse toItemResponse(OrderItem item) {
    return new OrderItemResponse(
        item.getId(),
        item.getProductName(),
        // ... 많은 필드들
    );
}
```

---

## 해야 할 일

### 1단계: 기존 MemberMapper 분석

먼저 기존 MapStruct 사용 패턴을 학습하세요:

```
src/main/java/platform/ecommerce/mapper/MemberMapper.java
```

```java
@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberResponse toResponse(Member member);

    @Mapping(target = "addresses", source = "addresses")
    MemberDetailResponse toDetailResponse(Member member);
}
```

### 2단계: OrderMapper 인터페이스 생성

**파일**: `mapper/OrderMapper.java`

```java
package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.OrderItem;
import platform.ecommerce.dto.response.order.OrderResponse;
import platform.ecommerce.dto.response.order.OrderItemResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Order -> OrderResponse 변환
    @Mapping(target = "status", source = "status.name")
    @Mapping(target = "items", source = "orderItems")
    OrderResponse toResponse(Order order);

    // OrderItem -> OrderItemResponse 변환
    @Mapping(target = "status", source = "status.name")
    OrderItemResponse toItemResponse(OrderItem item);

    // List 변환
    List<OrderItemResponse> toItemResponses(List<OrderItem> items);
}
```

### 3단계: 복잡한 매핑 처리

ShippingAddress 같은 중첩 객체는 별도 처리가 필요합니다:

```java
@Mapper(componentModel = "spring", uses = {ShippingAddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = "shippingAddress")
    OrderResponse toResponse(Order order);
}
```

또는 `@AfterMapping`을 사용:

```java
@AfterMapping
default void setFormattedDate(Order order, @MappingTarget OrderResponse.Builder response) {
    response.formattedDate(order.getOrderedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
}
```

### 4단계: OrderServiceImpl 수정

수동 변환 메서드를 MapStruct로 교체:

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;  // 추가!

    @Override
    public OrderResponse getOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        // Before: return toResponse(order);
        // After:
        return orderMapper.toResponse(order);
    }

    // toResponse() 메서드 삭제!
    // toItemResponse() 메서드 삭제!
}
```

---

## 참고: MapStruct 주요 어노테이션

| 어노테이션 | 설명 | 예시 |
|-----------|------|------|
| `@Mapper` | Mapper 인터페이스 정의 | `@Mapper(componentModel = "spring")` |
| `@Mapping` | 필드 매핑 규칙 | `@Mapping(target = "x", source = "y")` |
| `@Mappings` | 여러 매핑 그룹화 | 복수의 `@Mapping` |
| `@InheritInverseConfiguration` | 역방향 매핑 상속 | Request -> Entity |
| `@AfterMapping` | 매핑 후 커스텀 로직 | 복잡한 변환 |

---

## 테스트 작성

**파일**: `test/java/platform/ecommerce/mapper/OrderMapperTest.java`

```java
@SpringBootTest
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    @DisplayName("Order를 OrderResponse로 변환한다")
    void toResponse_success() {
        // given
        Order order = Order.builder()
            .id(1L)
            .orderNumber("ORD-2025-001")
            .status(OrderStatus.PAID)
            .totalAmount(new BigDecimal("50000"))
            .build();

        // when
        OrderResponse response = orderMapper.toResponse(order);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.orderNumber()).isEqualTo("ORD-2025-001");
        assertThat(response.status()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("OrderItem 목록을 변환한다")
    void toItemResponses_success() {
        // given
        List<OrderItem> items = List.of(
            createOrderItem(1L, "상품A"),
            createOrderItem(2L, "상품B")
        );

        // when
        List<OrderItemResponse> responses = orderMapper.toItemResponses(items);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).productName()).isEqualTo("상품A");
    }

    private OrderItem createOrderItem(Long id, String name) {
        // 테스트 픽스처
    }
}
```

---

## 수용 기준 (Acceptance Criteria)

- [ ] `OrderMapper.java` 생성 완료
- [ ] `OrderServiceImpl`에서 수동 변환 메서드 제거
- [ ] `OrderServiceImpl`에서 OrderMapper 주입 및 사용
- [ ] 기존 모든 OrderService 테스트 통과
- [ ] OrderMapper 단위 테스트 추가 (최소 3개)
- [ ] 컴파일 성공 (MapStruct 코드 생성 확인)

---

## 빌드 확인

MapStruct는 컴파일 타임에 코드를 생성합니다:

```bash
./gradlew compileJava
```

생성된 코드 확인:
```
build/generated/sources/annotationProcessor/java/main/
└── platform/ecommerce/mapper/OrderMapperImpl.java
```

---

## 수정/생성할 파일 목록

| 파일 | 타입 | 설명 |
|------|------|------|
| `mapper/OrderMapper.java` | NEW | MapStruct 매퍼 인터페이스 |
| `service/order/OrderServiceImpl.java` | MODIFY | OrderMapper 사용으로 변경 |
| `test/.../mapper/OrderMapperTest.java` | NEW | 매퍼 테스트 |

---

## 학습 포인트

1. **MapStruct 기본 사용법**: 컴파일 타임 코드 생성의 장점
2. **Spring 통합**: `componentModel = "spring"`으로 빈 등록
3. **복잡한 매핑**: `@Mapping`, `@AfterMapping` 활용
4. **일관성의 중요성**: 팀 내 동일한 패턴 사용의 장점
5. **보일러플레이트 제거**: 수동 코드 vs 자동 생성

---

## 추가 도전 과제 (Optional)

시간이 남으면 다음도 시도해보세요:

### 1. ShippingAddressMapper 생성

```java
@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {
    ShippingAddressResponse toResponse(ShippingAddress address);
}
```

### 2. 역방향 매핑 (Request -> Entity)

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Order toEntity(OrderCreateRequest request);

    @InheritInverseConfiguration
    OrderCreateRequest toRequest(Order order);
}
```

---

## 주의사항

- MapStruct는 **컴파일 타임**에 동작하므로, 수정 후 반드시 빌드 실행
- 기존 테스트가 모두 통과하는지 확인
- `OrderServiceImpl`의 다른 로직은 건드리지 마세요 (매핑만 변경)
- Null 처리: MapStruct는 기본적으로 null을 null로 매핑

---

## 참고 문서

- [MapStruct 공식 문서](https://mapstruct.org/documentation/stable/reference/html/)
- 프로젝트 내 예시: `MemberMapper.java`
