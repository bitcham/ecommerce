# Member Module Implementation Plan

## Goal
Implement a Member domain module that manages user registration, profile management, and delivery address handling with proper state transitions and business rule enforcement.

## Approach

### Domain Model Design
Use **Rich Domain Model** pattern where business logic resides in the entity itself.

```
Member (Aggregate Root)
├── MemberAddress (Entity, owned by Member)
├── MemberRole (Enum)
└── MemberStatus (Enum)
```

### Key Design Decisions

| Decision | Choice | Trade-off |
|----------|--------|-----------|
| Address ownership | Member owns addresses (cascade) | Simpler but couples lifecycle |
| Status management | Enum with behavior methods | Type-safe but less flexible than state pattern |
| Soft delete | SoftDeletable interface | Data retention vs storage cost |
| Email verification | Separate in Auth module | Cleaner separation but cross-module dependency |

### State Transitions

```
PENDING → ACTIVE (via email verification)
ACTIVE → SUSPENDED (admin action)
ACTIVE → WITHDRAWN (user action)
SUSPENDED → ACTIVE (admin action)
WITHDRAWN → PENDING (restore, requires re-verification)
```

### Business Rules

1. **Address Limit**: Max 10 addresses per member
2. **Default Address**: First address auto-defaults; only one default at a time
3. **Login Eligibility**: Only ACTIVE status can login
4. **Withdrawal**: Soft delete with timestamp, data retained
5. **Role Upgrade**: CUSTOMER → SELLER allowed; ADMIN cannot become SELLER

## Dependencies

- `BaseEntity` (common) - ID, audit fields
- `SoftDeletable` (common) - Soft delete interface
- `ErrorCode` (exception) - Centralized error codes
- `InvalidStateException` (exception) - State violation errors

## Impact Scope

| Component | Impact |
|-----------|--------|
| Auth Module | Depends on Member for user lookup |
| Order Module | References Member for orders |
| Review Module | References Member for reviews |
| Cart Module | References Member for cart ownership |

## File Structure

```
domain/member/
├── Member.java              # Aggregate root
├── MemberAddress.java       # Address entity
├── MemberRole.java          # Role enum
└── MemberStatus.java        # Status enum

repository/
├── MemberRepository.java           # JPA repository
├── MemberQueryRepository.java      # QueryDSL interface
└── MemberQueryRepositoryImpl.java  # QueryDSL impl

service/
├── MemberService.java       # Service interface
└── MemberServiceImpl.java   # Service implementation

controller/
└── MemberController.java    # REST controller

dto/
├── request/
│   ├── MemberCreateRequest.java
│   ├── MemberUpdateRequest.java
│   ├── MemberSearchCondition.java
│   ├── PasswordChangeRequest.java
│   ├── AddressCreateRequest.java
│   └── AddressUpdateRequest.java
└── response/
    ├── MemberResponse.java
    ├── MemberDetailResponse.java
    └── AddressResponse.java

mapper/
└── MemberMapper.java        # MapStruct mapper
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/members | Register member |
| GET | /api/v1/members | Search members |
| GET | /api/v1/members/{id} | Get member detail |
| PATCH | /api/v1/members/{id} | Update profile |
| PATCH | /api/v1/members/{id}/password | Change password |
| DELETE | /api/v1/members/{id} | Withdraw (soft delete) |
| POST | /api/v1/members/{id}/restore | Restore member |
| POST | /api/v1/members/{id}/addresses | Add address |
| PATCH | /api/v1/members/{id}/addresses/{addressId} | Update address |
| DELETE | /api/v1/members/{id}/addresses/{addressId} | Remove address |
| POST | /api/v1/members/{id}/addresses/{addressId}/default | Set default |

## Error Codes Required

| Code | Name | HTTP Status | Description |
|------|------|-------------|-------------|
| 3001 | MEMBER_NOT_FOUND | 404 | Member does not exist |
| 3002 | MEMBER_SUSPENDED | 403 | Account is suspended |
| 3003 | MEMBER_ALREADY_WITHDRAWN | 400 | Already withdrawn |
| 3004 | MEMBER_ADDRESS_NOT_FOUND | 404 | Address not found |
| 3005 | MEMBER_ADDRESS_LIMIT_EXCEEDED | 400 | Max 10 addresses |
| 3006 | MEMBER_EMAIL_DUPLICATED | 409 | Email already exists |

## Implementation Order

1. Domain entities (Member, MemberAddress, enums)
2. Repository interfaces
3. DTOs (Request/Response)
4. Mapper
5. Service layer
6. Controller layer
