# Admin Dashboard API Implementation Plan

## Goal
관리자가 주문, 상품, 회원, 매출 현황을 조회하고 관리할 수 있는 대시보드 API 구현.

## Approach

### 기능 범위
1. **대시보드 통계**: 오늘/이번주/이번달 매출, 주문수, 신규회원
2. **주문 관리**: 전체 주문 조회, 상태 변경, 검색
3. **상품 관리**: 전체 상품 조회, 승인/반려
4. **회원 관리**: 전체 회원 조회, 정지/해제

### API 설계
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/admin/dashboard/summary | 대시보드 요약 통계 |
| GET | /api/v1/admin/dashboard/sales | 기간별 매출 통계 |
| GET | /api/v1/admin/orders | 전체 주문 목록 |
| PATCH | /api/v1/admin/orders/{id}/status | 주문 상태 변경 |
| GET | /api/v1/admin/products | 전체 상품 목록 |
| PATCH | /api/v1/admin/products/{id}/approve | 상품 승인 |
| GET | /api/v1/admin/members | 전체 회원 목록 |
| PATCH | /api/v1/admin/members/{id}/suspend | 회원 정지 |

### 계층 구조
```
AdminController
    ├── AdminDashboardService (통계)
    ├── OrderService (주문 관리 - 기존 재사용)
    ├── ProductService (상품 관리 - 기존 재사용)
    └── MemberService (회원 관리 - 기존 재사용)
```

## Trade-offs

### 1. 통계 계산 방식
- **선택**: 실시간 쿼리 (COUNT, SUM)
- **대안**: 별도 통계 테이블 + 배치
- **이유**: MVP 단계에서 실시간 충분, 데이터 증가시 배치로 전환

### 2. 권한 체크
- **선택**: @PreAuthorize("hasRole('ADMIN')") 사용
- **이유**: Spring Security 표준 방식, 기존 RBAC 활용

### 3. 기존 Service 재사용
- **선택**: 관리자 전용 메서드 추가 vs 새 AdminService
- **결정**: 기존 Service에 관리자용 메서드 추가 (응집도 유지)

## Dependencies / Impact Scope

### Dependencies
- `OrderRepository`: 주문 통계/검색
- `ProductRepository`: 상품 통계/검색
- `MemberRepository`: 회원 통계/검색

### Impact
- 신규 파일:
  - `service/admin/AdminDashboardService.java`
  - `service/admin/AdminDashboardServiceImpl.java`
  - `controller/AdminController.java`
  - `dto/response/admin/*Response.java`
- 수정 파일: (필요시) 기존 Repository에 통계 쿼리 추가

### Error Handling
- `FORBIDDEN`: 관리자 권한 없음
- `ORDER_NOT_FOUND`: 주문 없음
- `MEMBER_NOT_FOUND`: 회원 없음
