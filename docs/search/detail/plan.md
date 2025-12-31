# Product Search Enhancement Plan

**Date**: 2025-12-28
**Feature**: Product Search Enhancement

## Goal

상품 검색 기능을 개선하여 키워드 검색(이름+설명), 다양한 정렬 옵션, 인기상품/추천상품 필터링 기능을 제공한다.

## Current State Analysis

### Existing Implementation
- `ProductSearchCondition`: keyword 필드 존재하지만 미사용
- `ProductQueryRepositoryImpl`: name만 검색, 정렬은 createdAt DESC 고정
- QueryDSL 기반 동적 쿼리

### Limitations
1. keyword 필드가 쿼리에서 무시됨
2. description 검색 불가
3. 정렬 옵션 없음 (최신순 고정)
4. 인기상품/추천상품 필터 없음

## Approach

### Phase 1: Enhanced Keyword Search (MVP)
1. keyword로 name + description 동시 검색 (OR 조건)
2. 다양한 정렬 옵션 지원
   - LATEST (최신순)
   - PRICE_LOW (낮은가격순)
   - PRICE_HIGH (높은가격순)
   - NAME_ASC (이름순)
3. 기존 name 필드는 정확한 이름 검색용으로 유지

### Phase 2: Future (Not in scope)
- Elasticsearch 연동 (대용량 데이터)
- 인기순 정렬 (주문/조회수 기반)
- 자동완성

## Trade-offs

| 항목 | 선택 | 이유 |
|------|------|------|
| 검색 엔진 | QueryDSL (유지) | MVP 단계, DB 기반으로 충분 |
| 정렬 | Enum 기반 | 타입 안전성, 확장 용이 |
| 키워드 검색 | OR 조건 | 사용자 편의성 (이름/설명 어디든 검색) |

## Implementation Details

### New Components
1. `ProductSortType.java` - 정렬 옵션 enum
2. `ProductSearchCondition` 확장 - sortType 필드 추가
3. `ProductQueryRepositoryImpl` 수정 - keyword/sort 로직 추가

### API Changes
- GET `/api/v1/products?keyword=xxx&sort=PRICE_LOW`
- 기존 API 호환성 유지 (sort 기본값: LATEST)

## Dependencies

- 기존 Product 엔티티 변경 없음
- ProductService 변경 최소화
- ProductController sort 파라미터 추가

## Risks

- 대용량 데이터에서 LIKE 검색 성능 → 인덱스 추가 고려
- description TEXT 컬럼 검색 → 필요시 Full-text index

## Checklist

- [ ] ProductSortType enum 생성
- [ ] ProductSearchCondition에 sortType 추가
- [ ] ProductQueryRepositoryImpl 수정
- [ ] ProductController sort 파라미터 추가
- [ ] 테스트 작성 및 통과
