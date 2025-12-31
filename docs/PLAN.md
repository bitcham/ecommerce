# Implementation Plan: Coupang Clone E-Commerce Platform

## Overview

이 문서는 쿠팡 클론 이커머스 플랫폼의 구현 계획을 단계별로 정의합니다.
CLAUDE.md의 워크플로우를 준수하며, TDD 방식으로 개발을 진행합니다.

---

## Phase 1: Foundation (MVP Core)

### 1.1 Project Setup

#### 1.1.1 Gradle 프로젝트 초기화
```
Goal: Spring Boot 4.0.1 + Java 25 기반 프로젝트 설정

Tasks:
├── build.gradle.kts 설정
│   ├── Spring Boot 4.0.1
│   ├── Spring Framework 7.0.x
│   ├── Kotlin 2.2.x (plugin)
│   ├── QueryDSL 5.x
│   ├── Flyway 10.x
│   └── 기타 의존성
├── application.yml 설정
│   ├── PostgreSQL 연결
│   ├── JPA/Hibernate 설정
│   ├── Flyway 설정
│   └── 로깅 설정
└── 기본 패키지 구조 생성
```

#### 1.1.2 Database Setup
```
Goal: PostgreSQL + Flyway 마이그레이션 설정

Tasks:
├── Docker Compose for PostgreSQL
├── Flyway 마이그레이션 스크립트
│   ├── V1__create_member_tables.sql
│   ├── V2__create_seller_tables.sql
│   ├── V3__create_product_tables.sql
│   └── ...
└── Testcontainers 설정
```

#### 1.1.3 Common Infrastructure
```
Goal: 공통 인프라 컴포넌트 구현

Tasks:
├── BaseEntity (id, createdAt, updatedAt)
├── SoftDeletable 인터페이스
├── ApiResponse 공통 응답 포맷
├── GlobalExceptionHandler
├── ErrorCode enum
└── BusinessException 계층
```

### 1.2 Member Module

#### 1.2.1 Domain Layer
```
Files:
├── domain/member/Member.java
├── domain/member/MemberAddress.java
├── domain/member/MemberRole.java (enum)
├── domain/member/MemberStatus.java (enum)
└── domain/member/EmailVerificationToken.java

Tests:
├── MemberTest.java (단위 테스트)
└── MemberAddressTest.java
```

#### 1.2.2 Repository Layer
```
Files:
├── repository/member/MemberRepository.java
├── repository/member/MemberQueryRepository.java
├── repository/member/MemberQueryRepositoryImpl.java
└── repository/member/MemberAddressRepository.java

Tests:
├── MemberRepositoryTest.java
└── MemberQueryRepositoryTest.java
```

#### 1.2.3 Service Layer
```
Files:
├── service/member/MemberService.java
└── service/member/MemberServiceImpl.java

Tests:
└── MemberServiceTest.java
```

#### 1.2.4 Controller Layer
```
Files:
├── controller/member/MemberController.java
├── dto/request/member/MemberUpdateRequest.java
└── dto/response/member/MemberResponse.java

Tests:
└── MemberControllerTest.java
```

### 1.3 Auth Module

#### 1.3.1 Security Configuration
```
Files:
├── config/SecurityConfig.java
├── config/properties/JwtProperties.java
├── security/jwt/JwtTokenProvider.java
├── security/jwt/JwtAuthenticationFilter.java
├── security/jwt/JwtAuthenticationEntryPoint.java
└── security/MemberDetails.java
```

#### 1.3.2 Auth Service
```
Files:
├── service/auth/AuthService.java
├── service/auth/AuthServiceImpl.java
├── service/auth/EmailVerificationService.java
├── dto/request/auth/LoginRequest.java
├── dto/request/auth/RegisterRequest.java
├── dto/response/auth/TokenResponse.java
└── controller/auth/AuthController.java

Tests:
├── AuthServiceTest.java
├── JwtTokenProviderTest.java
└── AuthControllerTest.java
```

### 1.4 Category Module

#### 1.4.1 Domain & Repository
```
Files:
├── domain/category/Category.java
├── repository/category/CategoryRepository.java
└── repository/category/CategoryQueryRepository.java

Tests:
├── CategoryTest.java
└── CategoryRepositoryTest.java
```

#### 1.4.2 Service & Controller
```
Files:
├── service/category/CategoryService.java
├── controller/category/CategoryController.java
├── dto/response/category/CategoryResponse.java
└── dto/response/category/CategoryTreeResponse.java

Tests:
├── CategoryServiceTest.java
└── CategoryControllerTest.java
```

### 1.5 Product Module

#### 1.5.1 Domain Layer
```
Files:
├── domain/product/Product.java
├── domain/product/ProductOption.java
├── domain/product/ProductImage.java
├── domain/product/ProductStatus.java (enum)
└── domain/product/DeliveryType.java (enum)

Tests:
├── ProductTest.java
├── ProductOptionTest.java
└── ProductImageTest.java
```

#### 1.5.2 Repository Layer
```
Files:
├── repository/product/ProductRepository.java
├── repository/product/ProductQueryRepository.java
├── repository/product/ProductQueryRepositoryImpl.java
├── repository/product/ProductOptionRepository.java
└── repository/product/ProductImageRepository.java

Tests:
├── ProductRepositoryTest.java
└── ProductQueryRepositoryTest.java
```

#### 1.5.3 Service Layer
```
Files:
├── service/product/ProductService.java
├── service/product/ProductServiceImpl.java
└── mapper/ProductMapper.java

Tests:
└── ProductServiceTest.java
```

#### 1.5.4 Controller Layer
```
Files:
├── controller/product/ProductController.java
├── dto/request/product/ProductCreateRequest.java
├── dto/request/product/ProductUpdateRequest.java
├── dto/response/product/ProductResponse.java
├── dto/response/product/ProductDetailResponse.java
└── dto/response/product/ProductListResponse.java

Tests:
└── ProductControllerTest.java
```

### 1.6 Seller Module

#### 1.6.1 Domain & Repository
```
Files:
├── domain/seller/Seller.java
├── domain/seller/SellerStatus.java (enum)
├── domain/seller/SellerGrade.java (enum)
├── repository/seller/SellerRepository.java
└── repository/seller/SellerQueryRepository.java

Tests:
├── SellerTest.java
└── SellerRepositoryTest.java
```

#### 1.6.2 Service & Controller
```
Files:
├── service/seller/SellerService.java
├── controller/seller/SellerController.java
├── dto/request/seller/SellerRegisterRequest.java
└── dto/response/seller/SellerResponse.java

Tests:
├── SellerServiceTest.java
└── SellerControllerTest.java
```

### 1.7 Cart Module

#### 1.7.1 Domain & Repository
```
Files:
├── domain/cart/Cart.java
├── domain/cart/CartItem.java
├── repository/cart/CartRepository.java
└── repository/cart/CartItemRepository.java

Tests:
├── CartTest.java
└── CartRepositoryTest.java
```

#### 1.7.2 Service & Controller
```
Files:
├── service/cart/CartService.java
├── controller/cart/CartController.java
├── dto/request/cart/CartItemAddRequest.java
├── dto/request/cart/CartItemUpdateRequest.java
└── dto/response/cart/CartResponse.java

Tests:
├── CartServiceTest.java
└── CartControllerTest.java
```

### 1.8 Order Module (Basic)

#### 1.8.1 Domain Layer
```
Files:
├── domain/order/Order.java
├── domain/order/OrderItem.java
├── domain/order/OrderStatus.java (enum)
└── domain/order/OrderCancelReason.java (enum)

Tests:
├── OrderTest.java
└── OrderItemTest.java
```

#### 1.8.2 Repository Layer
```
Files:
├── repository/order/OrderRepository.java
├── repository/order/OrderQueryRepository.java
└── repository/order/OrderItemRepository.java

Tests:
└── OrderRepositoryTest.java
```

#### 1.8.3 Service Layer
```
Files:
├── service/order/OrderService.java
└── service/order/OrderServiceImpl.java

Tests:
└── OrderServiceTest.java
```

#### 1.8.4 Controller Layer
```
Files:
├── controller/order/OrderController.java
├── dto/request/order/OrderCreateRequest.java
└── dto/response/order/OrderResponse.java

Tests:
└── OrderControllerTest.java
```

---

## Phase 2: Core Commerce

### 2.1 Payment Module

```
Goal: PG사 연동 및 결제 처리

Components:
├── domain/payment/Payment.java
├── domain/payment/PaymentStatus.java
├── service/payment/PaymentService.java
├── infrastructure/payment/PaymentGatewayClient.java (토스페이먼츠)
└── controller/payment/PaymentController.java
```

### 2.2 Delivery Module

```
Goal: 배송 상태 관리 및 추적

Components:
├── domain/delivery/Delivery.java
├── domain/delivery/DeliveryStatus.java
├── service/delivery/DeliveryService.java
└── controller/delivery/DeliveryController.java
```

### 2.3 Review Module

```
Goal: 리뷰 및 평점 시스템

Components:
├── domain/review/Review.java
├── domain/review/ReviewImage.java
├── service/review/ReviewService.java
├── controller/review/ReviewController.java
└── event/ReviewCreatedEvent.java (상품 평점 업데이트용)
```

### 2.4 Wishlist Module

```
Goal: 찜하기 기능

Components:
├── domain/wishlist/Wishlist.java
├── service/wishlist/WishlistService.java
└── controller/wishlist/WishlistController.java
```

---

## Phase 3: Enhancement

### 3.1 Search Module (Elasticsearch)

```
Goal: 상품 검색 및 자동완성

Components:
├── infrastructure/search/ElasticsearchConfig.java
├── infrastructure/search/ProductSearchRepository.java
├── service/search/SearchService.java
└── controller/search/SearchController.java
```

### 3.2 Coupon Module

```
Goal: 쿠폰 발급 및 적용

Components:
├── domain/coupon/Coupon.java
├── domain/coupon/MemberCoupon.java
├── service/coupon/CouponService.java
└── controller/coupon/CouponController.java
```

### 3.3 Notification Module

```
Goal: 알림 발송 및 조회

Components:
├── domain/notification/Notification.java
├── service/notification/NotificationService.java
├── infrastructure/notification/EmailNotificationSender.java
└── controller/notification/NotificationController.java
```

### 3.4 Admin Module

```
Goal: 관리자 기능

Components:
├── controller/admin/AdminMemberController.java
├── controller/admin/AdminSellerController.java
├── controller/admin/AdminProductController.java
├── controller/admin/AdminOrderController.java
└── controller/admin/AdminStatisticsController.java
```

---

## Phase 4: Scale & Optimize

### 4.1 Caching Layer (Redis)

```
Goal: 성능 최적화를 위한 캐싱

Components:
├── config/RedisConfig.java
├── service/cache/CacheService.java
└── @Cacheable 적용 (Product, Category 등)
```

### 4.2 Resilience (Resilience4j)

```
Goal: 장애 대응 패턴 적용

Components:
├── config/ResilienceConfig.java
├── Rate Limiting 적용
├── Circuit Breaker 적용 (외부 API)
└── Retry 적용
```

### 4.3 Monitoring

```
Goal: 모니터링 및 관측성

Components:
├── Micrometer + Prometheus 설정
├── 커스텀 비즈니스 메트릭
├── Structured Logging 설정
└── Health Check 엔드포인트
```

### 4.4 Security Hardening

```
Goal: 보안 강화

Components:
├── Rate Limiting 강화
├── SQL Injection 테스트
├── XSS 방어 테스트
├── CSRF 토큰 설정
└── Security Headers 설정
```

---

## Development Workflow (Per Feature)

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Feature Development Flow                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Create Feature Branch                                          │
│     └── git checkout -b feature/{feature-name}                     │
│                                                                     │
│  2. Write Documentation                                             │
│     ├── docs/{feature}/detail/plan.md                              │
│     └── docs/{feature}/test/test-plan.md                           │
│                                                                     │
│  3. TDD Cycle                                                       │
│     ├── Write failing tests                                         │
│     ├── Implement minimal code to pass                             │
│     └── Refactor                                                    │
│                                                                     │
│  4. Review Cycle                                                    │
│     ├── Senior Review → Improve → Test                             │
│     └── Lead Review → Improve → Test                               │
│                                                                     │
│  5. Final Refactor                                                  │
│     ├── Remove duplication                                          │
│     ├── Improve clarity                                             │
│     └── Final test run                                              │
│                                                                     │
│  6. Create Pull Request                                             │
│     └── Merge to main                                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Testing Strategy

### Test Types

| Type | Purpose | Tools |
|------|---------|-------|
| Unit Test | 개별 클래스/메서드 검증 | JUnit 5, Mockito |
| Integration Test | 컴포넌트 간 통합 검증 | Spring Test, Testcontainers |
| Repository Test | 데이터 접근 검증 | @DataJpaTest, Testcontainers |
| Controller Test | API 엔드포인트 검증 | MockMvc, RestAssured |
| E2E Test | 전체 흐름 검증 | Testcontainers |

### Test Coverage Goals

- Line Coverage: > 80%
- Branch Coverage: > 70%
- Critical Path Coverage: 100%

### Test Naming Convention

```java
// Pattern: {method}_{scenario}_{expectedResult}
void createOrder_withValidInput_shouldCreateOrderSuccessfully()
void createOrder_withEmptyCart_shouldThrowCartEmptyException()
void createOrder_withInsufficientStock_shouldThrowInsufficientStockException()
```

---

## Milestones

| Milestone | Target Date | Deliverables |
|-----------|-------------|--------------|
| M1: Project Setup | Week 1 | Gradle, DB, Common Infra |
| M2: Auth & Member | Week 2 | 회원가입, 로그인, JWT |
| M3: Product & Category | Week 3 | 상품 CRUD, 카테고리 |
| M4: Cart & Order | Week 4 | 장바구니, 주문 기본 |
| M5: Payment & Delivery | Week 5-6 | 결제, 배송 |
| M6: Review & Wishlist | Week 7 | 리뷰, 찜하기 |
| M7: Search & Coupon | Week 8-9 | 검색, 쿠폰 |
| M8: Admin & Notification | Week 10 | 관리자, 알림 |
| M9: Optimization | Week 11-12 | 캐싱, 모니터링, 보안 |

---

## Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| 기술 스택 호환성 (Spring Boot 4) | High | Medium | 의존성 버전 체크, 마이그레이션 가이드 참조 |
| PG 연동 복잡성 | Medium | Medium | 샌드박스 환경 먼저 테스트 |
| 성능 이슈 | High | Low | 초기부터 성능 테스트, 캐싱 전략 |
| 보안 취약점 | High | Low | OWASP 체크리스트 적용, 보안 테스트 |

---

## Coding Standards (코딩 표준)

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Class | PascalCase | `MemberService`, `OrderController` |
| Method | camelCase | `createOrder`, `findByEmail` |
| Variable | camelCase | `memberId`, `orderItems` |
| Constant | UPPER_SNAKE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| Package | lowercase | `platform.ecommerce.service` |
| Table | snake_case | `member_address`, `order_item` |
| Column | snake_case | `created_at`, `is_active` |

### Code Organization Rules

```
1. 클래스 멤버 순서:
   ├── static 상수
   ├── 인스턴스 필드
   ├── 생성자
   ├── static factory 메서드
   ├── public 메서드
   ├── protected 메서드
   ├── private 메서드
   └── equals/hashCode/toString

2. 메서드 길이: 최대 30줄 권장
3. 클래스 길이: 최대 300줄 권장
4. 파라미터 개수: 최대 5개, 초과 시 DTO 사용
5. Null 처리: Optional 사용 (Entity 필드 제외)
```

### Test Fixture Pattern

```java
// fixture/MemberFixture.java
public class MemberFixture {
    public static Member createActiveMember() {
        return Member.builder()
            .email("test@example.com")
            .password("encodedPassword")
            .name("테스트")
            .role(MemberRole.CUSTOMER)
            .status(MemberStatus.ACTIVE)
            .build();
    }

    public static Member createPendingMember() {
        return createActiveMember().toBuilder()
            .status(MemberStatus.PENDING)
            .build();
    }
}

// Usage in tests
@Test
void shouldActivatePendingMember() {
    // given
    Member member = MemberFixture.createPendingMember();

    // when
    member.activate();

    // then
    assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
}
```

### Design Principles

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Design Principles                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Domain-Driven Design (DDD)                                         │
│     ├── Rich Domain Model: 비즈니스 로직은 도메인 엔티티에              │
│     ├── Value Objects: 불변 객체로 개념 표현 (Money, Address)          │
│     ├── Aggregate Root: 일관성 경계 (Order → OrderItem)               │
│     └── Domain Events: 도메인 간 느슨한 결합                           │
│                                                                         │
│  2. Clean Architecture                                                  │
│     ├── Dependency Rule: 의존성은 안쪽으로만                           │
│     ├── Controller → Service → Repository → Domain                     │
│     └── Infrastructure는 가장 바깥쪽                                   │
│                                                                         │
│  3. SOLID Principles                                                    │
│     ├── SRP: 클래스는 하나의 책임만                                    │
│     ├── OCP: 확장에 열림, 수정에 닫힘                                  │
│     ├── LSP: 하위 타입 대체 가능                                       │
│     ├── ISP: 인터페이스 분리 (예: Reader/Writer 분리)                 │
│     └── DIP: 추상화에 의존 (구현체 주입)                               │
│                                                                         │
│  4. Fail Fast                                                           │
│     ├── Validation은 입력 단계에서                                     │
│     └── 잘못된 상태 조기 발견                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Document Info
- **Version**: 1.1.0
- **Created**: 2025-12-27
- **Updated**: 2025-12-27
- **Author**: AI Assistant
- **Reviewed By**: Senior Developer, Tech Lead
- **Status**: Approved
