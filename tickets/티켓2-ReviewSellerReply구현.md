# 티켓 2: Review Seller Reply 기능 구현

**Priority**: P1 (Important)
**Estimate**: 2시간
**Type**: New Feature
**담당**: 주니어 개발자

---

## 배경

PRD에서 P1 우선순위로 정의된 **판매자 답변 기능**이 아직 구현되지 않았습니다.

> "판매자 답변 | P1 | 리뷰에 대한 판매자 답변"
> — PRD 2.8 Review

SPC에서도 API가 정의되어 있습니다:

```
POST /api/v1/sellers/reviews/{id}/reply  # 리뷰 답변
```

---

## 현재 상태

### Review 도메인 (현재)

```java
// Review.java - seller reply 관련 필드 없음
@Entity
public class Review extends BaseEntity implements SoftDeletable {
    private Long memberId;
    private Long productId;
    private int rating;
    private String content;
    // ... sellerReply, sellerRepliedAt 없음!
}
```

### SPC 스키마 (목표)

```sql
-- review 테이블 (SPC에 정의됨)
seller_reply      TEXT,
seller_replied_at TIMESTAMP,
```

---

## 해야 할 일

### 1단계: Review 도메인에 필드 추가

**파일**: `domain/review/Review.java`

```java
@Entity
public class Review extends BaseEntity implements SoftDeletable {

    // 기존 필드들...

    // 추가: 판매자 답변
    @Column(name = "seller_reply", columnDefinition = "TEXT")
    private String sellerReply;

    @Column(name = "seller_replied_at")
    private LocalDateTime sellerRepliedAt;

    // Getter 추가
    public String getSellerReply() {
        return sellerReply;
    }

    public LocalDateTime getSellerRepliedAt() {
        return sellerRepliedAt;
    }

    /**
     * 판매자 답변 추가/수정
     */
    public void addSellerReply(String reply) {
        if (reply == null || reply.isBlank()) {
            throw new IllegalArgumentException("답변 내용은 필수입니다");
        }
        this.sellerReply = reply;
        this.sellerRepliedAt = LocalDateTime.now();
    }

    /**
     * 판매자 답변 삭제
     */
    public void removeSellerReply() {
        this.sellerReply = null;
        this.sellerRepliedAt = null;
    }

    /**
     * 판매자 답변 존재 여부
     */
    public boolean hasSellerReply() {
        return this.sellerReply != null && !this.sellerReply.isBlank();
    }
}
```

### 2단계: Flyway 마이그레이션 스크립트 추가

**파일**: `src/main/resources/db/migration/V{N}__add_seller_reply_to_review.sql`

```sql
-- Add seller reply columns to review table
ALTER TABLE review ADD COLUMN seller_reply TEXT;
ALTER TABLE review ADD COLUMN seller_replied_at TIMESTAMP;

COMMENT ON COLUMN review.seller_reply IS '판매자 답변 내용';
COMMENT ON COLUMN review.seller_replied_at IS '판매자 답변 작성 시간';
```

### 3단계: ReviewResponse DTO 수정

**파일**: `dto/response/review/ReviewResponse.java`

```java
public record ReviewResponse(
    Long id,
    Long productId,
    Long memberId,
    String memberName,
    int rating,
    String title,
    String content,
    List<String> images,
    int helpfulCount,
    boolean verified,
    LocalDateTime createdAt,

    // 추가
    String sellerReply,
    LocalDateTime sellerRepliedAt
) {}
```

### 4단계: SellerReplyRequest DTO 생성

**파일**: `dto/request/review/SellerReplyRequest.java`

```java
package platform.ecommerce.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerReplyRequest(
    @NotBlank(message = "답변 내용은 필수입니다")
    @Size(max = 1000, message = "답변은 1000자를 초과할 수 없습니다")
    String reply
) {}
```

### 5단계: ReviewService 수정

**파일**: `service/review/ReviewService.java`

```java
public interface ReviewService {
    // 기존 메서드들...

    // 추가
    ReviewResponse addSellerReply(Long reviewId, Long sellerId, SellerReplyRequest request);
    void removeSellerReply(Long reviewId, Long sellerId);
}
```

**파일**: `service/review/ReviewServiceImpl.java`

```java
@Override
@Transactional
public ReviewResponse addSellerReply(Long reviewId, Long sellerId, SellerReplyRequest request) {
    Review review = findReviewById(reviewId);

    // 상품 판매자 확인 (Product에서 sellerId 가져와서 비교)
    validateSellerOwnership(review.getProductId(), sellerId);

    review.addSellerReply(request.reply());
    log.info("Seller reply added to review: reviewId={}, sellerId={}", reviewId, sellerId);

    return toResponse(review);
}

@Override
@Transactional
public void removeSellerReply(Long reviewId, Long sellerId) {
    Review review = findReviewById(reviewId);
    validateSellerOwnership(review.getProductId(), sellerId);

    review.removeSellerReply();
    log.info("Seller reply removed from review: reviewId={}", reviewId);
}

private void validateSellerOwnership(Long productId, Long sellerId) {
    // Product의 sellerId와 비교
    // 현재 Seller 도메인이 없으므로, Product에서 sellerId 필드를 확인
    // 또는 SELLER 역할만 확인하는 방식으로 간소화
}
```

### 6단계: ReviewController에 엔드포인트 추가

**파일**: `controller/ReviewController.java`

```java
// 기존 코드 아래에 추가

// ========== Seller Reply Endpoints ==========

@Operation(summary = "Add seller reply", description = "Add or update seller reply to a review")
@PostMapping("/reviews/{reviewId}/reply")
@PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
public ApiResponse<ReviewResponse> addSellerReply(
        @Parameter(description = "Review ID") @PathVariable Long reviewId,
        @Valid @RequestBody SellerReplyRequest request
) {
    Long sellerId = SecurityUtils.getCurrentMemberId();
    ReviewResponse response = reviewService.addSellerReply(reviewId, sellerId, request);
    return ApiResponse.success(response);
}

@Operation(summary = "Remove seller reply", description = "Remove seller reply from a review")
@DeleteMapping("/reviews/{reviewId}/reply")
@ResponseStatus(HttpStatus.NO_CONTENT)
@PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
public void removeSellerReply(
        @Parameter(description = "Review ID") @PathVariable Long reviewId
) {
    Long sellerId = SecurityUtils.getCurrentMemberId();
    reviewService.removeSellerReply(reviewId, sellerId);
}
```

---

## 테스트 작성

**파일**: `test/java/platform/ecommerce/service/ReviewSellerReplyTest.java`

```java
@ExtendWith(MockitoExtension.class)
class ReviewSellerReplyTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Nested
    @DisplayName("판매자 답변 추가")
    class AddSellerReply {

        @Test
        @DisplayName("성공 - 판매자가 리뷰에 답변을 추가한다")
        void addSellerReply_success() {
            // given
            Review review = createReview();
            SellerReplyRequest request = new SellerReplyRequest("감사합니다!");

            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when
            ReviewResponse response = reviewService.addSellerReply(1L, 100L, request);

            // then
            assertThat(response.sellerReply()).isEqualTo("감사합니다!");
            assertThat(response.sellerRepliedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 빈 답변")
        void addSellerReply_emptyReply_throwsException() {
            // given
            Review review = createReview();
            SellerReplyRequest request = new SellerReplyRequest("");

            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.addSellerReply(1L, 100L, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("성공 - 기존 답변 수정")
        void addSellerReply_updateExisting() {
            // given
            Review review = createReview();
            review.addSellerReply("이전 답변");

            SellerReplyRequest request = new SellerReplyRequest("수정된 답변");
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when
            ReviewResponse response = reviewService.addSellerReply(1L, 100L, request);

            // then
            assertThat(response.sellerReply()).isEqualTo("수정된 답변");
        }
    }

    @Nested
    @DisplayName("판매자 답변 삭제")
    class RemoveSellerReply {

        @Test
        @DisplayName("성공 - 답변 삭제")
        void removeSellerReply_success() {
            // given
            Review review = createReview();
            review.addSellerReply("삭제할 답변");

            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            // when
            reviewService.removeSellerReply(1L, 100L);

            // then
            assertThat(review.hasSellerReply()).isFalse();
        }
    }

    private Review createReview() {
        return Review.builder()
                .memberId(1L)
                .productId(10L)
                .orderItemId(100L)
                .rating(5)
                .content("좋아요")
                .build();
    }
}
```

---

## 수용 기준 (Acceptance Criteria)

- [ ] Review 엔티티에 `sellerReply`, `sellerRepliedAt` 필드 추가
- [ ] `addSellerReply()`, `removeSellerReply()`, `hasSellerReply()` 메서드 구현
- [ ] Flyway 마이그레이션 스크립트 작성
- [ ] ReviewResponse에 seller reply 필드 추가
- [ ] SellerReplyRequest DTO 생성
- [ ] `POST /reviews/{reviewId}/reply` 엔드포인트 구현
- [ ] `DELETE /reviews/{reviewId}/reply` 엔드포인트 구현
- [ ] SELLER 또는 ADMIN 역할만 접근 가능
- [ ] 테스트 코드 작성 및 통과 (최소 5개)

---

## 수정/생성할 파일 목록

| 파일 | 타입 | 설명 |
|------|------|------|
| `domain/review/Review.java` | MODIFY | seller reply 필드/메서드 추가 |
| `db/migration/V{N}__add_seller_reply.sql` | NEW | DB 마이그레이션 |
| `dto/response/review/ReviewResponse.java` | MODIFY | seller reply 필드 추가 |
| `dto/request/review/SellerReplyRequest.java` | NEW | 요청 DTO |
| `service/review/ReviewService.java` | MODIFY | 메서드 시그니처 추가 |
| `service/review/ReviewServiceImpl.java` | MODIFY | 구현 추가 |
| `controller/ReviewController.java` | MODIFY | 엔드포인트 추가 |
| `test/.../ReviewSellerReplyTest.java` | NEW | 테스트 |

---

## 학습 포인트

1. **Rich Domain Model**: 비즈니스 로직을 엔티티에 캡슐화
2. **Flyway Migration**: 스키마 변경을 버전 관리
3. **@PreAuthorize**: 역할 기반 접근 제어 (SELLER, ADMIN)
4. **TDD**: 테스트 먼저 작성하는 습관

---

## 참고할 기존 코드

### Review 도메인 메서드 패턴

```java
// Review.java - update 메서드 참고
public void update(int rating, String title, String content, List<String> images) {
    validateRating(rating);
    this.rating = rating;
    // ...
}
```

### Controller 엔드포인트 패턴

```java
// ReviewController.java - updateReview 참고
@PatchMapping("/reviews/{reviewId}")
@PreAuthorize("isAuthenticated()")
public ApiResponse<ReviewResponse> updateReview(...) {
    Long memberId = SecurityUtils.getCurrentMemberId();
    // ...
}
```

---

## 주의사항

- Flyway 버전 번호는 기존 마이그레이션 다음 번호로 설정
- Seller 도메인이 없으므로 권한 검증은 **역할(SELLER) 기반**으로 단순화
- 기존 `toResponse()` 메서드에 seller reply 필드 매핑 추가 필요
- `application.yml`의 `ddl-auto: validate` 때문에 마이그레이션 필수
