# Seller Reply Feature - Test Plan

## Test Strategy

### 서비스 레이어 구조 (Application + Domain)

```
Controller
    ↓
SellerReplyApplicationService  ← DTO 변환, 오케스트레이션
    ↓
SellerReplyService (Domain)    ← 순수 비즈니스 로직, Entity 반환
    ↓
Repository
```

### 레이어별 테스트

| 레이어 | 테스트 유형 | Mock 대상 | 반환 타입 | 검증 항목 |
|--------|------------|-----------|----------|----------|
| **Entity** | Unit Test | - | - | 불변식, 비즈니스 메서드 |
| **Repository** | Integration Test | - | Entity | CRUD, 쿼리 |
| **DomainService** | Unit Test | Repository | **Entity** | 비즈니스 로직 |
| **ApplicationService** | Unit Test | DomainService, Mapper | **DTO** | DTO 변환, 이력 저장 |
| **Controller** | Integration Test | ApplicationService | DTO | API 응답, 권한 |

---

## Test Scenarios

### 1. SellerReply Entity Tests

```java
class SellerReplyTest {

    @Test
    void create_withValidData_shouldSucceed() {
        // Given: valid reviewId, sellerId, content
        // When: SellerReply.create(...)
        // Then: Entity created with correct values
    }

    @Test
    void create_withEmptyContent_shouldThrowException() {
        // Given: empty content
        // When: SellerReply.create(...)
        // Then: IllegalArgumentException
    }

    @Test
    void updateContent_byOwner_shouldSucceed() {
        // Given: existing reply, owner sellerId
        // When: reply.updateContent(newContent, sellerId)
        // Then: content updated
    }

    @Test
    void updateContent_byNonOwner_shouldThrowException() {
        // Given: existing reply, different sellerId
        // When: reply.updateContent(newContent, differentSellerId)
        // Then: UnauthorizedReplyException
    }

    @Test
    void delete_shouldSetDeletedAt() {
        // Given: existing reply
        // When: reply.delete()
        // Then: deletedAt is set
    }
}
```

### 2. SellerReplyRepository Tests

```java
@DataJpaTest
class SellerReplyRepositoryTest {

    @Test
    void findByReviewIdNotDeleted_whenExists_shouldReturnReply() {
        // Given: saved reply for reviewId
        // When: repository.findByReviewIdNotDeleted(reviewId)
        // Then: Optional with reply
    }

    @Test
    void findByReviewIdNotDeleted_whenDeleted_shouldReturnEmpty() {
        // Given: soft-deleted reply
        // When: repository.findByReviewIdNotDeleted(reviewId)
        // Then: Optional.empty()
    }

    @Test
    void existsByReviewIdAndDeletedAtIsNull_shouldReturnTrue() {
        // Given: saved reply for reviewId
        // When: repository.existsByReviewIdAndDeletedAtIsNull(reviewId)
        // Then: true
    }

    @Test
    void save_duplicateReviewId_shouldThrowException() {
        // Given: existing reply for reviewId
        // When: save another reply for same reviewId
        // Then: DataIntegrityViolationException (unique constraint)
    }
}
```

### 3. SellerReplyService (Domain Service) Tests
> **반환 타입: Entity**

```java
@ExtendWith(MockitoExtension.class)
class SellerReplyServiceTest {

    @Mock SellerReplyRepository replyRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock ProductRepository productRepository;

    @InjectMocks SellerReplyServiceImpl service;

    // === CREATE (returns Entity) ===
    @Test
    void createReply_withValidData_shouldReturnEntity() {
        // Given: valid reviewId, sellerId, content
        // When: service.createReply(reviewId, sellerId, content)
        // Then: SellerReply ENTITY returned (not DTO)
        assertThat(result).isInstanceOf(SellerReply.class);
    }

    @Test
    void createReply_whenReviewNotFound_shouldThrowException() {
        // Given: non-existent reviewId
        // When: service.createReply(...)
        // Then: ReviewNotFoundException
    }

    @Test
    void createReply_whenNotProductOwner_shouldThrowException() {
        // Given: sellerId != product.sellerId
        // When: service.createReply(...)
        // Then: UnauthorizedReplyException
    }

    @Test
    void createReply_whenAlreadyExists_shouldThrowException() {
        // Given: reply already exists for reviewId
        // When: service.createReply(...)
        // Then: DuplicateReplyException
    }

    // === READ (returns Entity) ===
    @Test
    void getReply_whenExists_shouldReturnEntity() {
        // Given: existing reply
        // When: service.getReply(reviewId)
        // Then: SellerReply ENTITY returned
    }

    @Test
    void getReply_whenNotExists_shouldThrowException() {
        // Given: no reply for reviewId
        // When: service.getReply(reviewId)
        // Then: ReplyNotFoundException
    }

    @Test
    void getReplyOptional_whenNotExists_shouldReturnEmpty() {
        // Given: no reply for reviewId
        // When: service.getReplyOptional(reviewId)
        // Then: Optional.empty()
    }

    // === UPDATE (returns Entity) ===
    @Test
    void updateReply_byOwner_shouldReturnUpdatedEntity() {
        // Given: existing reply, owner sellerId
        // When: service.updateReply(reviewId, sellerId, newContent)
        // Then: Updated SellerReply ENTITY returned
    }

    @Test
    void updateReply_byNonOwner_shouldThrowException() {
        // Given: existing reply, different sellerId
        // When: service.updateReply(...)
        // Then: UnauthorizedReplyException
    }

    // === DELETE ===
    @Test
    void deleteReply_byOwner_shouldSoftDelete() {
        // Given: existing reply
        // When: service.deleteReply(reviewId, sellerId)
        // Then: deletedAt is set
    }

    // === HISTORY (returns Entity list) ===
    @Test
    void getHistory_shouldReturnEntityList() {
        // Given: reply with update history
        // When: service.getHistory(reviewId)
        // Then: List<SellerReplyHistory> ENTITIES returned
    }
}
```

### 4. SellerReplyApplicationService Tests
> **반환 타입: DTO** (Mapper 사용)

```java
@ExtendWith(MockitoExtension.class)
class SellerReplyApplicationServiceTest {

    @Mock SellerReplyService domainService;  // Domain Service
    @Mock SellerReplyHistoryRepository historyRepository;
    @Mock SellerReplyMapper mapper;

    @InjectMocks SellerReplyApplicationService appService;

    // === CREATE (returns DTO) ===
    @Test
    void createReply_shouldDelegateToDomainServiceAndConvertToDTO() {
        // Given
        SellerReply entity = createTestReply();
        SellerReplyResponse dto = createTestResponse();
        when(domainService.createReply(anyLong(), anyLong(), anyString())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(dto);

        // When
        SellerReplyResponse result = appService.createReply(reviewId, sellerId, request);

        // Then
        verify(domainService).createReply(reviewId, sellerId, request.content());
        verify(mapper).toResponse(entity);
        assertThat(result).isInstanceOf(SellerReplyResponse.class);
    }

    // === READ (returns DTO) ===
    @Test
    void getReply_shouldConvertEntityToDTO() {
        // Given
        when(domainService.getReply(reviewId)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(dto);

        // When
        SellerReplyResponse result = appService.getReply(reviewId);

        // Then
        verify(mapper).toResponse(entity);
    }

    // === UPDATE (saves history + returns DTO) ===
    @Test
    void updateReply_shouldSaveHistoryAndConvertToDTO() {
        // Given
        SellerReply existingReply = createTestReply();
        String previousContent = existingReply.getContent();
        when(domainService.getReply(reviewId)).thenReturn(existingReply);
        when(domainService.updateReply(anyLong(), anyLong(), anyString())).thenReturn(updatedReply);

        // When
        appService.updateReply(reviewId, sellerId, request);

        // Then
        verify(historyRepository).save(argThat(history ->
            history.getPreviousContent().equals(previousContent)
        ));
        verify(mapper).toResponse(any());
    }

    // === HISTORY (returns DTO list) ===
    @Test
    void getHistory_shouldConvertEntitiesToDTOs() {
        // Given
        List<SellerReplyHistory> entities = List.of(history1, history2);
        when(domainService.getHistory(reviewId)).thenReturn(entities);

        // When
        List<SellerReplyHistoryResponse> result = appService.getHistory(reviewId);

        // Then
        verify(mapper, times(2)).toHistoryResponse(any());
    }
}
```

### 5. SellerReplyController Tests

```java
@WebMvcTest(SellerReplyController.class)
class SellerReplyControllerTest {

    @MockBean SellerReplyApplicationService appService;  // Application Service (not Domain)

    // === CREATE ===
    @Test
    @WithMockUser(roles = "SELLER")
    void createReply_withValidRequest_shouldReturn201() {
        // Given: valid request body
        // When: POST /api/v1/reviews/{reviewId}/reply
        // Then: 201 Created, SellerReplyResponse body
    }

    @Test
    @WithMockUser(roles = "USER")
    void createReply_withUserRole_shouldReturn403() {
        // Given: USER role (not SELLER)
        // When: POST /api/v1/reviews/{reviewId}/reply
        // Then: 403 Forbidden
    }

    // === READ ===
    @Test
    void getReply_shouldReturn200() {
        // Given: existing reply
        // When: GET /api/v1/reviews/{reviewId}/reply
        // Then: 200 OK, SellerReplyResponse body
    }

    @Test
    void getReply_whenNotExists_shouldReturn404() {
        // Given: no reply
        // When: GET /api/v1/reviews/{reviewId}/reply
        // Then: 404 Not Found
    }

    // === UPDATE ===
    @Test
    @WithMockUser(roles = "SELLER")
    void updateReply_byOwner_shouldReturn200() {
        // Given: owner seller
        // When: PUT /api/v1/reviews/{reviewId}/reply
        // Then: 200 OK, SellerReplyResponse body
    }

    // === DELETE ===
    @Test
    @WithMockUser(roles = "SELLER")
    void deleteReply_byOwner_shouldReturn204() {
        // Given: owner seller
        // When: DELETE /api/v1/reviews/{reviewId}/reply
        // Then: 204 No Content
    }

    // === HISTORY ===
    @Test
    @WithMockUser(roles = "SELLER")
    void getHistory_bySeller_shouldReturn200() {
        // Given: seller role
        // When: GET /api/v1/reviews/{reviewId}/reply/history
        // Then: 200 OK, List<SellerReplyHistoryResponse>
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHistory_byUser_shouldReturn403() {
        // Given: USER role
        // When: GET /api/v1/reviews/{reviewId}/reply/history
        // Then: 403 Forbidden
    }
}
```

---

## Edge Cases

### 답변 생성
- [ ] 삭제된 리뷰에 답변 시도 → `ReviewNotFoundException`
- [ ] 이미 답변이 있는 리뷰에 중복 답변 → `DuplicateReplyException`
- [ ] 본인 상품이 아닌 리뷰에 답변 → `UnauthorizedReplyException`
- [ ] 빈 내용으로 답변 → Validation Error (400)

### 답변 수정
- [ ] 삭제된 답변 수정 시도 → `ReplyNotFoundException`
- [ ] 다른 판매자가 수정 시도 → `UnauthorizedReplyException`
- [ ] 동일 내용으로 수정 → 이력 저장하지 않음

### 답변 삭제
- [ ] 이미 삭제된 답변 삭제 시도 → Idempotent (성공 처리)
- [ ] 다른 판매자가 삭제 시도 → `UnauthorizedReplyException`

### 리뷰 삭제 연동
- [ ] 리뷰 삭제 시 답변도 soft delete → Event Handler 테스트

---

## Service Layer Summary

| 서비스 | 역할 | 반환 타입 | Mock 대상 |
|--------|------|----------|----------|
| **SellerReplyService** (Domain) | 비즈니스 로직 | **Entity** | Repository |
| **SellerReplyApplicationService** | DTO 변환, 이력 저장 | **DTO** | DomainService, Mapper |

---

## Test Execution Order

1. **Entity Tests** - 도메인 로직 검증
2. **Repository Tests** - DB 연동 검증
3. **Domain Service Tests** - 비즈니스 로직 검증 (Entity 반환)
4. **Application Service Tests** - DTO 변환 검증
5. **Controller Tests** - API 응답 검증

---

## Success Criteria

- [ ] Entity 불변식 테스트 통과
- [ ] Repository CRUD 테스트 통과
- [ ] Domain Service 테스트 통과 (Entity 반환 검증, 커버리지 > 80%)
- [ ] Application Service 테스트 통과 (DTO 변환, 이력 저장 검증)
- [ ] Controller API 테스트 통과
- [ ] 권한 검증 테스트 통과 (SELLER만 작성/수정/삭제)
