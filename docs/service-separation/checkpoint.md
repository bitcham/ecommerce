# Service Layer Separation - Checkpoint

**Date:** 2026-01-07
**Status:** Completed (Documentation Updated)

## Summary

서비스 레이어를 Application Service + Domain Service로 분리하는 작업 완료. 원래 계획(3개 서비스만 분리)에서 전체 분리(12개 서비스)로 확장 적용됨.

## Changes Made

### Application Services Created (12개)

| Service | Pattern | Mapper | Side Effects |
|---------|---------|--------|--------------|
| `OrderApplicationService` | Full Separation | `OrderMapper` | Email, Notification |
| `AuthApplicationService` | Full Separation | `AuthMapper` | Email |
| `PaymentApplicationService` | Full Separation | `PaymentMapper` | Notification |
| `MemberApplicationService` | Mapper Integration | `MemberMapper` | - |
| `ProductApplicationService` | Mapper Integration | `ProductMapper` | Caching |
| `ReviewApplicationService` | Mapper Integration | `ReviewMapper` | - |
| `CouponApplicationService` | Mapper Integration | `CouponMapper` | - |
| `WishlistApplicationService` | Mapper Integration | `WishlistMapper` | - |
| `CartApplicationService` | Simple Delegation | - | - |
| `CategoryApplicationService` | Simple Delegation | - | - |
| `NotificationApplicationService` | Simple Delegation | - | - |
| `AdminDashboardApplicationService` | Simple Delegation | - | - |

### Mappers Created (7개 신규)

- `OrderMapper.java`
- `ProductMapper.java`
- `AuthMapper.java`
- `PaymentMapper.java`
- `CouponMapper.java`
- `ReviewMapper.java`
- `WishlistMapper.java`

### Result Objects Created (Auth)

- `LoginResult.java`
- `TokenResult.java`
- `RegistrationResult.java`
- `EmailNotificationInfo.java`

### Controllers Updated (12개)

모든 Controller가 ApplicationService를 의존하도록 변경됨.

## Architecture

```
Controller → ApplicationService → DomainService → Repository
                   ↓                    ↓
                Mapper              Entity
                   ↓
              EmailService / NotificationService
```

## Key Decisions

1. **전체 분리 적용**: 일관성을 위해 모든 서비스에 ApplicationService 도입
2. **점진적 마이그레이션**: Simple Delegation 패턴으로 시작, 필요시 Mapper 도입 가능
3. **부수효과 격리**: 이메일/알림 실패 시 트랜잭션 롤백 방지

## Verification

- [ ] All existing tests passing
- [ ] No API breaking changes
- [ ] Email/notification failures don't rollback transactions

## Next Steps

1. Simple Delegation 서비스들에 Mapper 점진적 도입 (Cart, Category, Notification, AdminDashboard)
2. ApplicationService 단위 테스트 추가
3. Mapper 단위 테스트 추가

## Related Documents

- [Plan](detail/plan.md)
- [Test Plan](test/test-plan.md)
