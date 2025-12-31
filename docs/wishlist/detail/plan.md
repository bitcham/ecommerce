# Wishlist Feature Implementation Plan

## Goal
회원이 관심 있는 상품을 찜 목록에 추가/삭제하고, 자신의 찜 목록을 조회할 수 있는 기능 구현.

## Approach

### 도메인 설계
```
Wishlist
├── id (PK)
├── member_id (FK, indexed)
├── product_id (FK, indexed)
├── created_at
└── updated_at

Unique Constraint: (member_id, product_id)
```

### API 설계
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/wishlist/{productId} | 찜 추가 |
| DELETE | /api/v1/wishlist/{productId} | 찜 삭제 |
| GET | /api/v1/wishlist | 내 찜 목록 조회 (페이징) |
| GET | /api/v1/wishlist/{productId}/check | 찜 여부 확인 |
| GET | /api/v1/wishlist/count | 찜 개수 조회 |

### 계층 구조
```
Controller → Service → Repository
     ↓           ↓
   DTO      Domain Entity
```

## Trade-offs

### 1. 상품 정보 포함 방식
- **선택**: Wishlist 조회 시 ProductService 호출하여 상품 정보 조합
- **대안**: Wishlist에 상품 정보 스냅샷 저장
- **이유**: 항상 최신 상품 정보 반영, 데이터 중복 방지

### 2. 삭제된 상품 처리
- **선택**: 애플리케이션 레벨에서 필터링 (soft delete 상품 제외)
- **대안**: DB CASCADE DELETE
- **이유**: 상품 도메인과 느슨한 결합 유지

### 3. 중복 추가 처리
- **선택**: DB unique constraint + 예외 처리
- **대안**: 추가 전 조회로 체크
- **이유**: Race condition 방지, DB 레벨 무결성 보장

## Dependencies / Impact Scope

### Dependencies
- `ProductService`: 상품 정보 조회
- `MemberService`: 회원 정보 (인증된 사용자)

### Impact
- 신규 테이블: `wishlist`
- 신규 파일:
  - `domain/wishlist/Wishlist.java`
  - `repository/WishlistRepository.java`
  - `service/wishlist/WishlistService.java`
  - `service/wishlist/WishlistServiceImpl.java`
  - `controller/WishlistController.java`
  - `dto/response/wishlist/WishlistResponse.java`
  - `dto/response/wishlist/WishlistItemResponse.java`

### Error Handling
- `WISHLIST_ALREADY_EXISTS`: 이미 찜한 상품
- `WISHLIST_NOT_FOUND`: 찜 목록에 없는 상품 삭제 시도
- `PRODUCT_NOT_FOUND`: 존재하지 않는 상품 찜 시도
