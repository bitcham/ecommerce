# Service Layer Separation Plan

## Goal
서비스 계층을 **ApplicationService**(DTO 변환, 오케스트레이션, 부수효과)와 **DomainService**(순수 비즈니스 로직)로 분리하여 관심사 분리 및 유지보수성 향상

## Approach

### 전체 분리 전략 (Full Separation for All)
모든 서비스에 ApplicationService를 도입하여 일관된 아키텍처 유지

### 네이밍 컨벤션
| 레이어 | 네이밍 | 역할 |
|--------|--------|------|
| Application | `{Domain}ApplicationService` | DTO 변환, 오케스트레이션, 부수효과(이메일, 알림) |
| Domain | `{Domain}Service` / `{Domain}ServiceImpl` | 순수 비즈니스 로직, Entity 반환 |
| Mapper | `{Domain}Mapper` | MapStruct 기반 DTO 변환 |

### 현재 구현 상태 (3가지 패턴)

#### 1. Full Separation (with Side Effects)
완전 분리 + 부수효과 처리

| 서비스 | 부수효과 | Mapper |
|--------|----------|--------|
| `OrderApplicationService` | 이메일, 알림 발송 | `OrderMapper` |
| `AuthApplicationService` | 인증 이메일 발송 | `AuthMapper` |
| `PaymentApplicationService` | 결제 알림 | `PaymentMapper` |

**특징:**
- DomainService → Entity/Result 객체 반환
- ApplicationService → Mapper로 DTO 변환 + 이메일/알림 처리
- 트랜잭션 경계 명확

#### 2. Mapper Integration
Mapper를 통한 DTO 변환

| 서비스 | Mapper |
|--------|--------|
| `MemberApplicationService` | `MemberMapper` |
| `ProductApplicationService` | `ProductMapper` |
| `ReviewApplicationService` | `ReviewMapper` |
| `CouponApplicationService` | `CouponMapper` |
| `WishlistApplicationService` | `WishlistMapper` |

**특징:**
- DomainService → Entity 반환
- ApplicationService → Mapper로 DTO 변환
- 캐싱 처리 (ProductApplicationService)

#### 3. Simple Delegation (점진적 마이그레이션 대상)
단순 위임 패턴 (Facade)

| 서비스 | 상태 |
|--------|------|
| `CartApplicationService` | DomainService가 아직 DTO 반환 |
| `CategoryApplicationService` | DomainService가 아직 DTO 반환 |
| `NotificationApplicationService` | DomainService가 아직 DTO 반환 |
| `AdminDashboardApplicationService` | DomainService가 아직 DTO 반환 |

**특징:**
- ApplicationService가 DomainService에 단순 위임
- 추후 Mapper 도입 시 변경 용이

## Trade-offs

### 전체 분리 선택 이유
1. **일관성**: 모든 Controller가 ApplicationService만 의존
2. **점진적 마이그레이션**: Simple Delegation에서 Full Separation으로 점진적 전환 가능
3. **테스트 용이성**: DomainService는 순수 단위 테스트 가능
4. **부수효과 격리**: 이메일, 알림 등 부수효과가 ApplicationService에 집중

### 대안 비교
| 접근법 | 장점 | 단점 | 선택 여부 |
|--------|------|------|----------|
| **Hybrid (3개만)** | 최소 변경 | 일관성 부족 | ✗ |
| **Full Separation** | 일관된 아키텍처 | 파일 수 증가 | ✓ 선택 |
| **Mapper Only** | 빠른 적용 | 부수효과 분리 불가 | ✗ |

## Dependencies / Impact Scope

### 아키텍처 흐름
```
Controller → ApplicationService → DomainService → Repository
                   ↓                    ↓
                Mapper              Entity
                   ↓
              EmailService / NotificationService (부수효과)
```

### 변경된 컴포넌트

#### Application Services (12개)
```
service/application/
├── OrderApplicationService.java      # Full Separation + Side Effects
├── AuthApplicationService.java       # Full Separation + Side Effects
├── PaymentApplicationService.java    # Full Separation + Side Effects
├── MemberApplicationService.java     # Mapper Integration
├── ProductApplicationService.java    # Mapper Integration + Caching
├── ReviewApplicationService.java     # Mapper Integration
├── CouponApplicationService.java     # Mapper Integration
├── WishlistApplicationService.java   # Mapper Integration
├── CartApplicationService.java       # Simple Delegation
├── CategoryApplicationService.java   # Simple Delegation
├── NotificationApplicationService.java # Simple Delegation
└── AdminDashboardApplicationService.java # Simple Delegation
```

#### Mappers (8개)
```
mapper/
├── MemberMapper.java      # 기존
├── OrderMapper.java       # 신규
├── ProductMapper.java     # 신규
├── AuthMapper.java        # 신규
├── PaymentMapper.java     # 신규
├── CouponMapper.java      # 신규
├── ReviewMapper.java      # 신규
└── WishlistMapper.java    # 신규
```

#### Controllers (변경됨)
모든 Controller가 ApplicationService를 의존하도록 변경:
- `OrderController` → `OrderApplicationService`
- `AuthController` → `AuthApplicationService`
- `MemberController` → `MemberApplicationService`
- `ProductController` → `ProductApplicationService`
- (기타 모든 Controller 동일 패턴)

### 하위 호환성
- Controller API는 변경 없음 (Response DTO 동일)
- 기존 테스트는 ApplicationService mock으로 수정 필요

## Package Structure

```
service/
├── application/                           # Application Services (12개)
│   ├── OrderApplicationService.java
│   ├── AuthApplicationService.java
│   ├── PaymentApplicationService.java
│   ├── MemberApplicationService.java
│   ├── ProductApplicationService.java
│   ├── ReviewApplicationService.java
│   ├── CouponApplicationService.java
│   ├── WishlistApplicationService.java
│   ├── CartApplicationService.java
│   ├── CategoryApplicationService.java
│   ├── NotificationApplicationService.java
│   └── AdminDashboardApplicationService.java
├── order/                                 # Domain Services
│   ├── OrderService.java
│   └── OrderServiceImpl.java
├── auth/                                  # Auth 관련 Result 객체
│   ├── LoginResult.java
│   ├── TokenResult.java
│   ├── RegistrationResult.java
│   └── EmailNotificationInfo.java
├── product/
│   ├── ProductService.java
│   └── ProductServiceImpl.java
├── cart/
│   ├── CartService.java
│   └── CartServiceImpl.java
└── ...

mapper/
├── MemberMapper.java
├── OrderMapper.java
├── ProductMapper.java
├── AuthMapper.java
├── PaymentMapper.java
├── CouponMapper.java
├── ReviewMapper.java
└── WishlistMapper.java
```

## 향후 마이그레이션 계획

### Simple Delegation → Mapper Integration
1. `CartService` Entity 반환으로 변경
2. `CartMapper` 생성
3. `CartApplicationService`에 Mapper 통합
4. 동일 패턴으로 Category, Notification, AdminDashboard 적용
