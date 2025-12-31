# Wishlist Module Checkpoint

**Date**: 2025-12-28
**Feature**: Wishlist (찜하기)

## Completed Work

### Domain Layer
- **Wishlist.java**: Entity for wishlist items
  - Unique constraint (member_id, product_id)
  - Factory method `Wishlist.of(memberId, productId)`
  - Extends BaseTimeEntity for audit fields

### Repository Layer
- **WishlistRepository.java**: JPA Repository
  - `findByMemberIdAndProductId`: 찜 조회
  - `existsByMemberIdAndProductId`: 중복 체크
  - `findByMemberIdOrderByCreatedAtDesc`: 페이징 목록
  - `countByMemberId`: 찜 개수

### Service Layer
- **WishlistService.java**: Service interface
- **WishlistServiceImpl.java**: Service implementation
  - `addToWishlist`: 중복/상품 검증 후 저장
  - `removeFromWishlist`: 찜 삭제
  - `getMyWishlist`: 상품 정보 포함 페이징 조회
  - `isInWishlist`: 찜 여부 확인
  - `getWishlistCount`: 찜 개수

### Controller Layer
- **WishlistController.java**: REST API
  - POST `/api/v1/wishlist/{productId}`: 찜 추가
  - DELETE `/api/v1/wishlist/{productId}`: 찜 삭제
  - GET `/api/v1/wishlist`: 목록 조회
  - GET `/api/v1/wishlist/{productId}/check`: 여부 확인
  - GET `/api/v1/wishlist/count`: 개수 조회

### DTOs
- **WishlistResponse.java**: 기본 응답
- **WishlistItemResponse.java**: 상품 정보 포함 응답

### Database Migration
- **V11__create_wishlist_table.sql**: 테이블 및 인덱스

### Tests
- **WishlistServiceTest.java**: 12 unit tests
  - AddToWishlist: 정상/중복/상품없음
  - RemoveFromWishlist: 정상/없음
  - GetMyWishlist: 정상/빈목록/삭제상품
  - IsInWishlist: true/false
  - GetWishlistCount: 정상/0

## Design Decisions

1. **중복 체크 우선**: 상품 조회보다 저렴한 exists 쿼리 먼저 실행
2. **삭제 상품 처리**: try-catch로 graceful degradation
3. **느슨한 결합**: Product와 FK 없이 productId만 저장

## TDD Process Followed
1. ✅ plan.md 작성
2. ✅ test-plan.md 작성
3. ✅ 테스트 먼저 작성 → 실패 확인
4. ✅ 구현 → 테스트 통과
5. ✅ Senior Review (코드 품질)
6. ✅ Lead Review (아키텍처/보안)

## Known Issues
- memberId를 @RequestParam으로 받는 방식 (기존 패턴 준수, 향후 보안 개선 필요)
