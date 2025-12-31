# Wishlist Feature Test Plan

## Unit Tests

### WishlistServiceTest

#### addToWishlist
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 정상 추가 | memberId, productId | WishlistResponse 반환 | High |
| 중복 추가 시도 | 이미 찜한 productId | ConflictException | High |
| 존재하지 않는 상품 | 없는 productId | EntityNotFoundException | High |

#### removeFromWishlist
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 정상 삭제 | memberId, productId | void (성공) | High |
| 찜하지 않은 상품 삭제 | 없는 wishlist item | EntityNotFoundException | Medium |

#### getMyWishlist
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 정상 조회 | memberId, pageable | Page<WishlistItemResponse> | High |
| 빈 목록 조회 | 찜 없는 memberId | 빈 Page | Medium |
| 삭제된 상품 필터링 | deleted 상품 포함 | 삭제된 상품 제외 | Medium |

#### isInWishlist
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 찜한 상품 확인 | 찜한 productId | true | High |
| 찜하지 않은 상품 확인 | 찜 안한 productId | false | High |

#### getWishlistCount
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 정상 카운트 | memberId | 찜 개수 | Medium |
| 빈 목록 카운트 | 찜 없는 memberId | 0 | Low |

### WishlistRepositoryTest

| Test Case | Expected | Priority |
|-----------|----------|----------|
| findByMemberIdAndProductId 조회 | Optional<Wishlist> | High |
| existsByMemberIdAndProductId 존재 확인 | boolean | High |
| findByMemberIdOrderByCreatedAtDesc 페이징 | Page<Wishlist> | High |
| countByMemberId 카운트 | long | Medium |
| deleteByMemberIdAndProductId 삭제 | void | High |

## Integration Tests

### WishlistControllerTest

| Test Case | Method | Endpoint | Expected Status |
|-----------|--------|----------|-----------------|
| 찜 추가 성공 | POST | /api/v1/wishlist/{productId} | 201 Created |
| 인증 없이 추가 시도 | POST | /api/v1/wishlist/{productId} | 401 Unauthorized |
| 찜 삭제 성공 | DELETE | /api/v1/wishlist/{productId} | 204 No Content |
| 찜 목록 조회 | GET | /api/v1/wishlist | 200 OK |
| 찜 여부 확인 | GET | /api/v1/wishlist/{productId}/check | 200 OK |
| 찜 개수 조회 | GET | /api/v1/wishlist/count | 200 OK |

## Edge Cases

1. **동시성**: 동일 상품 동시 찜 시도 → DB unique constraint로 하나만 성공
2. **대용량**: 찜 목록이 많을 때 페이징 성능 (인덱스 확인)
3. **상품 삭제**: 찜한 상품이 삭제되었을 때 목록에서 제외

## Test Fixtures

```java
// 테스트용 상수
Long MEMBER_ID = 1L;
Long PRODUCT_ID = 100L;
Long WISHLIST_ID = 1000L;

// 테스트 Wishlist 생성
Wishlist testWishlist = Wishlist.of(MEMBER_ID, PRODUCT_ID);
```
