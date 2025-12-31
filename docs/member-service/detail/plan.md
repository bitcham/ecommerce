# Member Service Layer Implementation Plan

## Goal
Implement Repository, Service, and Controller layers for Member module with proper transaction management, validation, and REST API endpoints.

## Approach

### Layer Architecture

```
Controller (REST API)
    ↓ DTO
Service (Business Logic, Transaction)
    ↓ Entity
Repository (Data Access)
    ↓ JPA/QueryDSL
Database
```

### Key Design Decisions

| Decision | Choice | Trade-off |
|----------|--------|-----------|
| Repository pattern | Spring Data JPA + QueryDSL | Standard but adds complexity for dynamic queries |
| DTO mapping | MapStruct | Compile-time safety vs runtime flexibility |
| Validation | Bean Validation (@Valid) | Declarative but limited for complex rules |
| Transaction | Service layer @Transactional | Clear boundary but requires careful rollback handling |

### Components

#### Repository Layer
- `MemberRepository`: JPA repository with custom QueryDSL
- `MemberQueryRepository`: Interface for dynamic queries
- `MemberQueryRepositoryImpl`: QueryDSL implementation

#### Service Layer
- `MemberService`: Interface defining business operations
- `MemberServiceImpl`: Implementation with transaction management

#### Controller Layer
- `MemberController`: REST endpoints for member CRUD

#### DTO Layer
- Request DTOs with validation annotations
- Response DTOs for API responses
- MapStruct mapper for entity-DTO conversion

## Dependencies

- Member domain (completed)
- Common infrastructure (ApiResponse, ErrorCode, Exceptions)
- Spring Security (for password encoding)

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/members | Register | Public |
| GET | /api/v1/members | Search members | Admin |
| GET | /api/v1/members/{id} | Get detail | Self/Admin |
| PATCH | /api/v1/members/{id} | Update profile | Self |
| PATCH | /api/v1/members/{id}/password | Change password | Self |
| DELETE | /api/v1/members/{id} | Withdraw | Self |
| POST | /api/v1/members/{id}/restore | Restore | Admin |
| POST | /api/v1/members/{id}/addresses | Add address | Self |
| PATCH | /api/v1/members/{id}/addresses/{aid} | Update address | Self |
| DELETE | /api/v1/members/{id}/addresses/{aid} | Remove address | Self |

## File Structure

```
repository/
├── MemberRepository.java
├── MemberQueryRepository.java
└── MemberQueryRepositoryImpl.java

service/
├── MemberService.java
└── MemberServiceImpl.java

controller/
└── MemberController.java

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
└── MemberMapper.java
```

## Error Handling

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Member not found | EntityNotFoundException | 404 |
| Duplicate email | DuplicateResourceException | 409 |
| Password mismatch | InvalidStateException | 400 |
| Invalid state transition | InvalidStateException | 400 |
| Address limit exceeded | InvalidStateException | 400 |

## Implementation Order

1. DTOs (Request/Response)
2. Mapper (MapStruct)
3. Repository (JPA + QueryDSL)
4. Service (Business logic)
5. Controller (REST API)
