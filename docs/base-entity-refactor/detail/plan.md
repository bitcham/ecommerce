# BaseEntity Refactoring Plan

## Goal

5개의 엔티티(`Wishlist`, `Notification`, `PasswordResetToken`, `RefreshToken`, `EmailVerificationToken`)를 `BaseTimeEntity`에서 `BaseEntity`로 변경하고, 사용되지 않는 `BaseTimeEntity` 클래스를 삭제한다.

## Background

### 현재 문제점

1. **코드 중복**: 5개 엔티티가 `BaseTimeEntity` 상속 + 자체 `id` 필드 정의
2. **equals/hashCode 누락**: `BaseEntity`에만 있는 동등성 비교가 5개 엔티티에 없음
3. **일관성 부재**: 동일한 패턴인데 다른 부모 클래스 사용

### 영향받는 엔티티

| 엔티티 | 파일 경로 |
|--------|----------|
| Wishlist | `domain/wishlist/Wishlist.java` |
| Notification | `domain/notification/Notification.java` |
| PasswordResetToken | `domain/auth/PasswordResetToken.java` |
| RefreshToken | `domain/auth/RefreshToken.java` |
| EmailVerificationToken | `domain/auth/EmailVerificationToken.java` |

## Approach

### Option 1: 단순 상속 변경 (Recommended)

각 엔티티에서:
- `extends BaseTimeEntity` → `extends BaseEntity`
- 자체 `id` 필드 및 관련 어노테이션 제거

**Pros:**
- 구현 간단
- 코드 중복 제거
- equals/hashCode 자동 적용

**Cons:**
- 기존 테스트 코드 수정 필요할 수 있음

### Option 2: BaseEntity가 BaseTimeEntity 상속

```java
public abstract class BaseTimeEntity { createdAt, updatedAt }
public abstract class BaseEntity extends BaseTimeEntity { id, equals, hashCode }
```

**Pros:**
- 향후 확장성 (id 없이 시간만 필요한 케이스 대응)
- 계층 구조 명확

**Cons:**
- 현재 BaseTimeEntity 사용처 없음 (YAGNI 위반)
- 불필요한 복잡성

### 선택: Option 1

현재 코드베이스에서 `BaseTimeEntity`를 의도대로 사용하는 곳이 없으므로, 단순하게 삭제하는 것이 YAGNI 원칙에 부합함.

## Implementation Steps

### Phase 1: 엔티티 수정

1. `Wishlist.java` 수정
   - `extends BaseTimeEntity` → `extends BaseEntity`
   - `id` 필드 및 `@Id`, `@GeneratedValue` 제거

2. `Notification.java` 수정 (동일)

3. `PasswordResetToken.java` 수정 (동일)

4. `RefreshToken.java` 수정 (동일)

5. `EmailVerificationToken.java` 수정 (동일)

### Phase 2: BaseTimeEntity 삭제

6. `BaseTimeEntity.java` 삭제
7. `QBaseTimeEntity.java` (QueryDSL 생성 파일) - 빌드 시 자동 정리

### Phase 3: 검증

8. 컴파일 확인
9. 테스트 실행
10. QueryDSL Q클래스 재생성 확인

## Dependencies / Impact Scope

### 직접 영향
- 5개 엔티티 파일 수정
- 1개 베이스 클래스 삭제

### 잠재적 영향
- Repository 계층: 영향 없음 (id 타입 Long 유지)
- Service 계층: 영향 없음 (인터페이스 변경 없음)
- 테스트 코드: equals/hashCode 동작 변경으로 일부 영향 가능

### DB 스키마
- 변경 없음 (컬럼 구조 동일)

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| equals/hashCode 동작 변경 | 기존 Set/Map 사용처 테스트로 검증 |
| QueryDSL Q클래스 이슈 | clean build로 재생성 |

## Rollback Plan

Git revert로 즉시 롤백 가능 (DB 스키마 변경 없음)
