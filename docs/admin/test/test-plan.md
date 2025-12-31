# Admin Dashboard Test Plan

## Unit Tests

### AdminDashboardServiceTest

#### getDashboardSummary
| Test Case | Expected | Priority |
|-----------|----------|----------|
| 정상 조회 | 오늘 매출, 주문수, 신규회원 반환 | High |
| 데이터 없음 | 0으로 반환 | Medium |

#### getSalesStatistics
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 일별 매출 조회 | period=DAILY | 7일간 일별 매출 | High |
| 주별 매출 조회 | period=WEEKLY | 4주간 주별 매출 | High |
| 월별 매출 조회 | period=MONTHLY | 12개월 월별 매출 | High |

#### getOrderStatistics
| Test Case | Expected | Priority |
|-----------|----------|----------|
| 상태별 주문 카운트 | 각 상태별 개수 Map | High |

#### getTopProducts
| Test Case | Expected | Priority |
|-----------|----------|----------|
| 인기 상품 TOP 10 | 판매량 순 정렬 | Medium |

### AdminOrderServiceTest (기존 OrderService 확장)

#### getAllOrders
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 전체 조회 | pageable | Page<OrderResponse> | High |
| 상태 필터 | status=PAID | 해당 상태만 | High |
| 기간 필터 | from, to | 기간 내 주문만 | Medium |

#### updateOrderStatus
| Test Case | Input | Expected | Priority |
|-----------|-------|----------|----------|
| 정상 상태 변경 | orderId, newStatus | 변경된 OrderResponse | High |
| 잘못된 상태 전환 | DELIVERED → PENDING | InvalidStateException | High |

### AdminMemberServiceTest (기존 MemberService 확장)

#### getAllMembers
| Test Case | Expected | Priority |
|-----------|----------|----------|
| 전체 조회 | Page<MemberResponse> | High |
| 검색 필터 | 이름/이메일 검색 | Medium |

#### suspendMember / unsuspendMember
| Test Case | Expected | Priority |
|-----------|----------|----------|
| 정상 정지 | status=SUSPENDED | High |
| 정상 해제 | status=ACTIVE | High |
| 이미 정지된 회원 정지 | InvalidStateException | Medium |

## Integration Tests

### AdminControllerTest

| Test Case | Method | Endpoint | Expected |
|-----------|--------|----------|----------|
| 관리자 대시보드 조회 | GET | /admin/dashboard/summary | 200 OK |
| 비관리자 접근 | GET | /admin/dashboard/summary | 403 Forbidden |
| 인증 없이 접근 | GET | /admin/dashboard/summary | 401 Unauthorized |
| 전체 주문 조회 | GET | /admin/orders | 200 OK |
| 주문 상태 변경 | PATCH | /admin/orders/{id}/status | 200 OK |
| 전체 회원 조회 | GET | /admin/members | 200 OK |
| 회원 정지 | PATCH | /admin/members/{id}/suspend | 200 OK |

## Test Fixtures

```java
// 테스트용 통계 데이터
BigDecimal TODAY_SALES = BigDecimal.valueOf(1500000);
long TODAY_ORDERS = 25;
long TODAY_NEW_MEMBERS = 10;

// 기간별 매출 데이터
List<DailySales> dailySales = List.of(
    new DailySales(LocalDate.now(), BigDecimal.valueOf(100000)),
    new DailySales(LocalDate.now().minusDays(1), BigDecimal.valueOf(150000))
);
```
