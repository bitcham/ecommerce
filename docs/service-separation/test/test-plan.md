# Service Layer Separation - Test Plan

## Test Strategy

### 레이어별 테스트 전략

| 레이어 | 테스트 유형 | Mock 대상 | 검증 항목 |
|--------|------------|-----------|----------|
| **DomainService** | Unit Test | Repository | 비즈니스 로직, Entity 상태 변경 |
| **ApplicationService** | Unit Test | DomainService, Mapper | DTO 변환, 오케스트레이션, 부수효과 호출 |
| **Mapper** | Unit Test | - | 필드 매핑 정확성 |
| **Controller** | Integration Test | ApplicationService | API 응답 형식 |

### 패턴별 테스트 접근법

#### 1. Full Separation (Order, Auth, Payment)
```
DomainService Unit Test
    ↓
ApplicationService Unit Test (Mock: DomainService, EmailService, NotificationService)
    ↓
Mapper Unit Test
    ↓
Controller Integration Test
```

#### 2. Mapper Integration (Member, Product, Review, Coupon, Wishlist)
```
DomainService Unit Test
    ↓
ApplicationService Unit Test (Mock: DomainService)
    ↓
Mapper Unit Test
    ↓
Controller Integration Test
```

#### 3. Simple Delegation (Cart, Category, Notification, AdminDashboard)
```
DomainService Unit Test (현재 DTO 반환)
    ↓
ApplicationService Unit Test (단순 위임 검증)
    ↓
Controller Integration Test
```

## Test Scenarios

### 1. OrderApplicationService Tests (Full Separation)

```java
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock OrderService orderService;
    @Mock OrderMapper orderMapper;
    @Mock EmailService emailService;
    @Mock NotificationService notificationService;
    @Mock MemberRepository memberRepository;

    @InjectMocks OrderApplicationService applicationService;

    @Test
    void createOrder_shouldDelegateToOrderService() {
        // Given
        Order order = createTestOrder();
        when(orderService.createOrder(anyLong(), any())).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(createOrderResponse());

        // When
        OrderResponse response = applicationService.createOrder(1L, request);

        // Then
        verify(orderService).createOrder(eq(1L), any());
        verify(orderMapper).toResponse(order);
    }

    @Test
    void processPayment_shouldSendNotifications() {
        // Given
        Order order = createPaidOrder();
        Member member = createTestMember();
        when(orderService.processPayment(anyLong(), any(), anyString())).thenReturn(order);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));

        // When
        applicationService.processPayment(1L, PaymentMethod.CARD, "TX123");

        // Then
        verify(emailService).sendOrderConfirmationEmail(anyString(), anyString(), anyString());
        verify(notificationService).notifyOrderStatusChange(anyLong(), anyString(), anyString());
    }

    @Test
    void processPayment_shouldNotRollbackOnEmailFailure() {
        // Given
        when(emailService.sendOrderConfirmationEmail(any(), any(), any()))
            .thenThrow(new RuntimeException("Email failed"));

        // When/Then: Should not throw, just log warning
        assertDoesNotThrow(() ->
            applicationService.processPayment(1L, PaymentMethod.CARD, "TX123")
        );
    }
}
```

### 2. OrderService (Domain) Tests

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Test
    void createOrder_shouldReturnOrderEntity() {
        // Given: Valid memberId and OrderCreateRequest
        // When: orderService.createOrder(memberId, request)
        // Then: Returns Order entity (not DTO)
    }

    @Test
    void createOrder_shouldDecreaseStock() {
        // Given: Order with items
        // When: orderService.createOrder(...)
        // Then: ProductService.decreaseStock called for each item
    }

    @Test
    void cancelOrder_shouldRestoreStock() {
        // Given: PAID order with items
        // When: orderService.cancelOrder(orderId, memberId, reason)
        // Then: Stock restored for each item
    }
}
```

### 3. AuthApplicationService Tests (Full Separation)

```java
@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock AuthService authService;
    @Mock AuthMapper authMapper;
    @Mock MemberMapper memberMapper;
    @Mock EmailService emailService;

    @InjectMocks AuthApplicationService applicationService;

    @Test
    void register_shouldSendVerificationEmail() {
        // Given
        RegistrationResult result = new RegistrationResult(member, "token123");
        when(authService.register(any())).thenReturn(result);

        // When
        applicationService.register(request);

        // Then
        verify(emailService).sendVerificationEmail(
            eq(member.getEmail()),
            eq(member.getName()),
            eq("token123")
        );
    }

    @Test
    void requestPasswordReset_shouldSendResetEmail() {
        // Given
        EmailNotificationInfo info = new EmailNotificationInfo("test@email.com", "Name", "resetToken");
        when(authService.createPasswordResetToken(anyString())).thenReturn(info);

        // When
        applicationService.requestPasswordReset("test@email.com");

        // Then
        verify(emailService).sendPasswordResetEmail(
            eq("test@email.com"),
            eq("Name"),
            eq("resetToken")
        );
    }
}
```

### 4. ProductApplicationService Tests (Mapper Integration + Caching)

```java
@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    @Mock ProductService productService;
    @Mock ProductMapper productMapper;

    @InjectMocks ProductApplicationService applicationService;

    @Test
    void getProduct_shouldUseMapper() {
        // Given
        Product product = createTestProduct();
        when(productService.getProduct(1L)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(createProductResponse());

        // When
        ProductResponse response = applicationService.getProduct(1L);

        // Then
        verify(productMapper).toResponse(product);
        assertNotNull(response);
    }

    @Test
    void updateProduct_shouldEvictCache() {
        // Note: Cache eviction verified via integration test
        // Unit test verifies delegation
        when(productService.updateProduct(anyLong(), any())).thenReturn(product);

        applicationService.updateProduct(1L, request);

        verify(productService).updateProduct(1L, request);
    }
}
```

### 5. Mapper Tests

```java
class OrderMapperTest {

    private OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void toResponse_shouldMapAllFields() {
        // Given
        Order order = createTestOrder();

        // When
        OrderResponse response = mapper.toResponse(order);

        // Then
        assertEquals(order.getId(), response.id());
        assertEquals(order.getOrderNumber(), response.orderNumber());
        assertEquals(order.getStatus(), response.status());
    }

    @Test
    void toShippingAddressResponse_shouldCalculateFullAddress() {
        // Given
        ShippingAddress address = new ShippingAddress("서울시", "101호");

        // When
        ShippingAddressResponse response = mapper.toShippingAddressResponse(address);

        // Then
        assertEquals("서울시 101호", response.fullAddress());
    }
}

class ProductMapperTest {

    @Test
    void toDetailResponse_shouldIncludeOptionsAndImages() {
        // Given
        Product product = createProductWithOptionsAndImages();

        // When
        ProductDetailResponse response = mapper.toDetailResponse(product);

        // Then
        assertFalse(response.options().isEmpty());
        assertFalse(response.images().isEmpty());
    }
}
```

### 6. CartApplicationService Tests (Simple Delegation)

```java
@ExtendWith(MockitoExtension.class)
class CartApplicationServiceTest {

    @Mock CartService cartService;

    @InjectMocks CartApplicationService applicationService;

    @Test
    void getOrCreateCart_shouldDelegateToCartService() {
        // Given
        CartResponse expected = createCartResponse();
        when(cartService.getOrCreateCart(1L)).thenReturn(expected);

        // When
        CartResponse result = applicationService.getOrCreateCart(1L);

        // Then
        verify(cartService).getOrCreateCart(1L);
        assertEquals(expected, result);
    }
}
```

## Edge Cases

### Order Creation
- [ ] Empty items list - should throw validation error
- [ ] Insufficient stock - should throw stock error before saving
- [ ] Invalid member ID - should throw EntityNotFoundException

### Payment Processing
- [ ] Already paid order - should throw InvalidStateException
- [ ] Cancelled order - should throw InvalidStateException
- [ ] Email send failure - should NOT rollback transaction (부수효과 격리)

### Order Cancellation
- [ ] Already cancelled - should throw InvalidStateException
- [ ] Shipped order - should throw InvalidStateException
- [ ] Partial cancellation - should restore only that item's stock

### Authentication
- [ ] Duplicate email registration - should throw DuplicateEmailException
- [ ] Invalid verification token - should throw InvalidTokenException
- [ ] Expired password reset token - should throw TokenExpiredException

## Test Execution Order

1. **Mapper Tests** - 가장 먼저 (의존성 없음)
2. **DomainService Unit Tests** - Repository mock
3. **ApplicationService Unit Tests** - DomainService mock
4. **Integration Tests** - 전체 플로우 검증

## Success Criteria

### Unit Test Coverage
- [ ] DomainService 커버리지 > 80%
- [ ] ApplicationService 커버리지 > 80%
- [ ] Mapper 커버리지 > 90%

### Integration Test
- [ ] 모든 기존 Controller 테스트 통과
- [ ] API 응답 형식 변경 없음 확인

### Side Effects Verification
- [ ] 이메일 발송 실패 시 트랜잭션 롤백 안됨 확인
- [ ] 알림 발송 실패 시 트랜잭션 롤백 안됨 확인

## Test Configuration

### Mock 설정
```java
@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }
}
```

### 테스트 데이터 빌더
```java
public class TestDataBuilder {

    public static Order createTestOrder() {
        return Order.builder()
            .id(1L)
            .orderNumber("ORD-001")
            .status(OrderStatus.PENDING)
            .build();
    }

    public static Member createTestMember() {
        return Member.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .build();
    }
}
```
