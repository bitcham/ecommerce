# Seller Reply Feature - Implementation Plan

## Goal
리뷰에 대한 판매자 답변 기능 구현. 기존 Embedded 방식에서 별도 SellerReply Aggregate로 마이그레이션하여 DDD 원칙 준수 및 수정 이력 관리.

## Current State (AS-IS)
기존 `review` 테이블에 Embedded 방식으로 존재:
```sql
-- review 테이블 내 컬럼
seller_reply        TEXT,
seller_replied_at   TIMESTAMP,
```

## Target State (TO-BE)
별도 `seller_reply` Aggregate로 분리:
```
SellerReply (Aggregate Root)     SellerReplyHistory
├── id                           ├── id
├── reviewId (ID 참조)           ├── sellerReplyId
├── sellerId                     ├── previousContent
├── content                      ├── modifiedBy
├── deletedAt                    └── modifiedAt
└── createdAt/updatedAt
```

## Approach

### 마이그레이션 전략
1. **V12**: 새 테이블 생성 + 데이터 마이그레이션 + 기존 컬럼 제거

### 선택 이유
1. **Actor 분리**: Review(고객) vs SellerReply(판매자)
2. **수정 이력 관리**: 분쟁 대응을 위한 History 테이블
3. **확장성**: 응답률 통계, 알림 등 추가 기능 용이
4. **기존 패턴 일치**: Review가 이미 ID 참조 방식 사용

## Trade-offs

| 고려사항 | 선택 | 이유 |
|---------|------|------|
| 모델링 | 별도 Aggregate | DDD 원칙, Actor 분리 |
| 이력 관리 | SellerReplyHistory 테이블 | 분쟁 대응, 감사 추적 |
| 답변 개수 | 리뷰당 1개 (unique constraint) | E-commerce 표준 패턴 |
| 삭제 처리 | Soft Delete + Event | Review 삭제 시 연동 |

## Dependencies / Impact Scope

### Flyway Migration
| 파일 | 내용 |
|------|------|
| `V12__create_seller_reply_tables.sql` | seller_reply, seller_reply_history 테이블 생성 + 데이터 마이그레이션 + review 테이블에서 기존 컬럼 제거 |

### 신규 생성 (Java)
| 파일 | 설명 |
|------|------|
| `domain/review/SellerReply.java` | Aggregate Root |
| `domain/review/SellerReplyHistory.java` | 수정 이력 Entity |
| `repository/review/SellerReplyRepository.java` | Repository |
| `repository/review/SellerReplyHistoryRepository.java` | 이력 Repository |
| `service/review/SellerReplyService.java` | Interface |
| `service/review/SellerReplyServiceImpl.java` | Domain Service |
| `service/application/SellerReplyApplicationService.java` | Application Service |
| `controller/SellerReplyController.java` | REST Controller |
| `dto/request/SellerReplyRequest.java` | Request DTO |
| `dto/response/SellerReplyResponse.java` | Response DTO |
| `mapper/SellerReplyMapper.java` | MapStruct Mapper |

### 수정
| 파일 | 변경 내용 |
|------|----------|
| `domain/review/Review.java` | sellerReply, sellerRepliedAt 필드 제거 |
| `service/review/ReviewServiceImpl.java` | 삭제 시 ReviewDeletedEvent 발행 |
| `dto/response/ReviewResponse.java` | sellerReply 필드 타입 변경 (String → SellerReplyResponse) |

## Error Handling

| 에러 | 분류 | HTTP | 처리 |
|------|------|------|------|
| 리뷰 없음 | Expected | 404 | `ReviewNotFoundException` |
| 중복 답변 | Expected | 409 | `DuplicateReplyException` |
| 권한 없음 | Expected | 403 | `UnauthorizedReplyException` |
| 답변 없음 | Expected | 404 | `ReplyNotFoundException` |

## API Endpoints

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/v1/reviews/{reviewId}/reply` | 답변 작성 | SELLER |
| GET | `/api/v1/reviews/{reviewId}/reply` | 답변 조회 | ALL |
| PUT | `/api/v1/reviews/{reviewId}/reply` | 답변 수정 | SELLER (본인) |
| DELETE | `/api/v1/reviews/{reviewId}/reply` | 답변 삭제 | SELLER (본인) |
| GET | `/api/v1/reviews/{reviewId}/reply/history` | 이력 조회 | SELLER/ADMIN |

## Database Schema

### V12 Migration SQL
```sql
-- 1. seller_reply 테이블 생성
CREATE TABLE seller_reply (
    id              BIGSERIAL PRIMARY KEY,
    review_id       BIGINT NOT NULL REFERENCES review(id),
    seller_id       BIGINT NOT NULL REFERENCES member(id),
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT uq_seller_reply_review UNIQUE (review_id)
);

-- 2. seller_reply_history 테이블 생성
CREATE TABLE seller_reply_history (
    id                  BIGSERIAL PRIMARY KEY,
    seller_reply_id     BIGINT NOT NULL REFERENCES seller_reply(id),
    previous_content    TEXT NOT NULL,
    modified_by         BIGINT NOT NULL REFERENCES member(id),
    modified_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 인덱스 생성
CREATE INDEX idx_seller_reply_review ON seller_reply(review_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_seller_reply_seller ON seller_reply(seller_id);
CREATE INDEX idx_seller_reply_history_reply ON seller_reply_history(seller_reply_id);

-- 4. 기존 데이터 마이그레이션 (seller_reply가 있는 경우)
INSERT INTO seller_reply (review_id, seller_id, content, created_at, updated_at)
SELECT r.id, p.seller_id, r.seller_reply, COALESCE(r.seller_replied_at, r.created_at), r.updated_at
FROM review r
JOIN product p ON r.product_id = p.id
WHERE r.seller_reply IS NOT NULL AND r.deleted_at IS NULL;

-- 5. review 테이블에서 기존 컬럼 제거
ALTER TABLE review DROP COLUMN seller_reply;
ALTER TABLE review DROP COLUMN seller_replied_at;
```
