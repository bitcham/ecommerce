# Seller Reply Feature - Checkpoint

**Date:** 2026-01-07
**Status:** Completed

## Summary

리뷰에 대한 판매자 답변 기능을 별도 Aggregate로 구현 완료. 수정 이력 관리 및 권한 기반 접근 제어 포함.

## Key Decisions

1. **Option B: 별도 SellerReply Aggregate** 선택
   - Review(고객)와 SellerReply(판매자)는 서로 다른 Actor
   - 독립적 생명주기 및 불변식 보유
   - ID 참조로 느슨한 결합 유지

2. **수정 이력 관리 방식**
   - 최초 생성 시 이력 없음 (previousContent가 NULL일 수 없음)
   - 수정 시에만 이력 생성
   - FK CASCADE 없이 설계 → 답변 삭제 후에도 감사 이력 보존

3. **이력 접근 권한**
   - 공개 API: `isEdited` 플래그만 표시
   - 상세 이력: 해당 상품 소유 SELLER 또는 ADMIN만 조회 가능

## Files Created

### Domain Layer
| File | Description |
|------|-------------|
| `domain/review/SellerReply.java` | Aggregate Root (Factory Method, Soft Delete) |
| `domain/review/SellerReplyHistory.java` | 수정 이력 Entity |

### Repository Layer
| File | Description |
|------|-------------|
| `repository/review/SellerReplyRepository.java` | Partial Index 활용 쿼리 |
| `repository/review/SellerReplyHistoryRepository.java` | 이력 조회 쿼리 |

### Service Layer
| File | Description |
|------|-------------|
| `service/review/SellerReplyService.java` | Domain Service Interface |
| `service/review/SellerReplyServiceImpl.java` | Domain Service (Entity 반환) |
| `service/application/SellerReplyApplicationService.java` | Application Service (DTO 변환, 권한 검증) |

### DTO & Mapper
| File | Description |
|------|-------------|
| `dto/request/SellerReplyRequest.java` | 생성/수정 요청 DTO |
| `dto/response/SellerReplyResponse.java` | 응답 DTO (isEdited 포함) |
| `dto/response/SellerReplyHistoryResponse.java` | 이력 응답 DTO |
| `mapper/SellerReplyMapper.java` | MapStruct Mapper |

### Exception
| File | Description |
|------|-------------|
| `exception/DuplicateReplyException.java` | 중복 답변 예외 |
| `exception/UnauthorizedReplyException.java` | 권한 없음 예외 |

### Controller
| File | Description |
|------|-------------|
| `controller/SellerReplyController.java` | REST API (/api/v1/reviews/{reviewId}/reply) |

### Migration
| File | Description |
|------|-------------|
| `db/migration/V12__create_seller_reply_tables.sql` | 테이블 생성 + 데이터 마이그레이션 |

### Test
| File | Description |
|------|-------------|
| `service/SellerReplyServiceTest.java` | Domain Service 단위 테스트 (18개) |

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/reviews/{reviewId}/reply` | 답변 작성 | SELLER |
| GET | `/api/v1/reviews/{reviewId}/reply` | 답변 조회 | PUBLIC |
| PUT | `/api/v1/reviews/{reviewId}/reply` | 답변 수정 | SELLER (owner) |
| DELETE | `/api/v1/reviews/{reviewId}/reply` | 답변 삭제 | SELLER (owner) |
| GET | `/api/v1/reviews/{reviewId}/reply/history` | 이력 조회 | SELLER (owner) / ADMIN |

## Architecture

```
Controller
    ↓ (DTO)
ApplicationService ──→ SellerReplyMapper
    │                       ↓
    │               SellerReplyResponse
    ↓ (Entity)
DomainService ──→ SellerReplyHistoryRepository (이력 저장)
    ↓
Repository
    ↓
SellerReply (Aggregate Root)
```

## Database Design

```sql
seller_reply
├── id (PK)
├── review_id (FK, UNIQUE)
├── seller_id (FK)
├── content (TEXT)
├── created_at, updated_at
└── deleted_at (Soft Delete)

seller_reply_history
├── id (PK)
├── seller_reply_id (NO FK - 감사 이력 보존)
├── previous_content (TEXT)
├── modified_by (FK)
└── modified_at
```

**Indexes:**
- `idx_seller_reply_review` (Partial: WHERE deleted_at IS NULL)
- `idx_seller_reply_seller`
- `idx_seller_reply_history_reply`
- `idx_seller_reply_history_modified`

## Verification

- [x] Domain Service 단위 테스트 (18개) 통과
- [x] Senior Review 완료 (중복 검증 코드 제거)
- [x] Lead Review 완료 (아키텍처, 보안 검증)
- [x] Flyway 마이그레이션 작성

## Issues Found & Fixed

1. **Entity 상속 오류**: `BaseTimeEntity` → `BaseEntity` 변경 (getId() 메서드 필요)
2. **테스트 Product 생성 오류**: `basePrice` 필드 누락 → 추가
3. **중복 소유권 검증**: ServiceImpl에서 Entity 메서드 호출 전 중복 검증 → 제거

## Next Steps

1. ApplicationService 통합 테스트 추가
2. ReviewResponse에 SellerReply 포함 (optional)
3. ReviewDeletedEvent 발행 시 답변 자동 삭제 연동

## Related Documents

- [Plan](detail/plan.md)
- [Test Plan](test/test-plan.md)
