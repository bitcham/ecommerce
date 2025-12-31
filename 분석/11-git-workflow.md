# 11. Git 워크플로우 (Git Workflow)

> **학습 목표**: 팀에서 사용하는 Git 브랜치 전략과 커밋 컨벤션을 이해하고, 협업에 필요한 Git 명령어를 익힙니다.

---

## 1. 브랜치 전략

### 1.1 브랜치 구조

```
main (production)
  │
  ├── develop (integration)
  │     │
  │     ├── feature/member-auth
  │     ├── feature/order-payment
  │     └── feature/product-search
  │
  ├── release/v1.2.0
  │
  └── hotfix/critical-bug-fix
```

### 1.2 브랜치 유형

| 브랜치 | 용도 | 생성 원본 | 병합 대상 |
|--------|------|----------|----------|
| `main` | 프로덕션 배포 | - | - |
| `develop` | 개발 통합 | `main` | `main` (릴리즈 시) |
| `feature/*` | 기능 개발 | `develop` | `develop` |
| `release/*` | 릴리즈 준비 | `develop` | `main`, `develop` |
| `hotfix/*` | 긴급 버그 수정 | `main` | `main`, `develop` |

### 1.3 브랜치 네이밍 규칙

```
feature/{기능명}           # feature/member-registration
feature/{이슈번호}-{설명}   # feature/123-add-login-api
bugfix/{버그설명}          # bugfix/cart-quantity-error
hotfix/{긴급수정}          # hotfix/payment-gateway-fix
release/{버전}             # release/v1.2.0
```

---

## 2. 개발 워크플로우

### 2.1 기능 개발 흐름

**참조**: `CLAUDE.md` Workflow Summary

```
1. 문서 작성
   └── docs/{feature}/detail/plan.md
   └── docs/{feature}/test/test-plan.md

2. 브랜치 생성
   └── feature/{기능명}

3. TDD 개발
   └── 테스트 작성 → 실패 확인 → 구현 → 통과

4. 코드 리뷰
   └── Senior Review → Lead Review → Refactor

5. PR & Merge
   └── develop 브랜치로 병합

6. 완료 기록
   └── checkpoint.md 작성
```

### 2.2 Git 명령어 시퀀스

```bash
# 1. develop 브랜치 최신화
git checkout develop
git pull origin develop

# 2. feature 브랜치 생성
git checkout -b feature/member-registration

# 3. 작업 & 커밋
git add .
git commit -m "feat(member): add registration endpoint"

# 4. 원격에 푸시
git push -u origin feature/member-registration

# 5. PR 생성 (GitHub)
# → develop 브랜치로 PR 요청

# 6. 리뷰 후 병합
# → GitHub에서 Squash and merge
```

---

## 3. 커밋 컨벤션

### 3.1 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 3.2 Type 종류

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새 기능 추가 | `feat(auth): add JWT token refresh` |
| `fix` | 버그 수정 | `fix(order): resolve quantity calculation` |
| `refactor` | 리팩토링 | `refactor(member): extract validation logic` |
| `test` | 테스트 추가/수정 | `test(cart): add edge case tests` |
| `docs` | 문서 수정 | `docs(readme): update installation guide` |
| `style` | 코드 스타일 (포맷팅) | `style: apply code formatting` |
| `chore` | 빌드, 설정 변경 | `chore: update gradle dependencies` |
| `perf` | 성능 개선 | `perf(query): add index for search` |

### 3.3 Scope 예시

```
member, order, product, cart, payment, auth, config, security, api
```

### 3.4 좋은 커밋 메시지 예시

```bash
# ✅ 좋은 예시
feat(member): implement email verification flow

- Add EmailVerificationToken entity
- Create verification email template
- Add verification endpoint in AuthController

Closes #123

# ❌ 나쁜 예시
update code
fix bug
WIP
```

### 3.5 커밋 단위

| 권장 | 비권장 |
|------|--------|
| 하나의 논리적 변경 = 하나의 커밋 | 여러 기능을 한 커밋에 |
| 컴파일/테스트 가능한 상태 | 깨진 상태로 커밋 |
| 명확한 목적 | "WIP", "temp" 커밋 |

---

## 4. Pull Request (PR)

### 4.1 PR 템플릿

```markdown
## 요약
회원 가입 시 이메일 인증 기능 추가

## 변경 사항
- [x] EmailVerificationToken 엔티티 추가
- [x] 인증 메일 발송 서비스 구현
- [x] 인증 확인 API 엔드포인트 추가
- [x] 단위 테스트 작성

## 테스트
- [x] EmailVerificationTokenTest 통과
- [x] AuthServiceTest 통과
- [x] 통합 테스트 통과

## 리뷰 요청 사항
- 토큰 만료 시간(24시간) 적절한지 검토 부탁드립니다
- 이메일 템플릿 내용 확인 부탁드립니다

## 관련 이슈
Closes #123
```

### 4.2 PR 체크리스트

**작성자**:
- [ ] 자체 코드 리뷰 완료
- [ ] 테스트 통과 확인
- [ ] 불필요한 주석/로그 제거
- [ ] 충돌 해결 완료

**리뷰어**:
- [ ] 로직 정확성 검토
- [ ] 보안 취약점 확인
- [ ] 성능 이슈 검토
- [ ] 테스트 커버리지 확인

### 4.3 PR 머지 전략

| 전략 | 사용 시점 | 결과 |
|------|----------|------|
| **Squash and merge** | feature → develop | 깔끔한 히스토리 |
| **Merge commit** | develop → main | 히스토리 보존 |
| **Rebase and merge** | 거의 사용 안함 | 선형 히스토리 |

---

## 5. 충돌 해결

### 5.1 충돌 발생 시 워크플로우

```bash
# 1. develop 브랜치 최신화
git checkout develop
git pull origin develop

# 2. feature 브랜치로 돌아가서 rebase
git checkout feature/my-feature
git rebase develop

# 3. 충돌 발생 시 파일 수정
# 에디터에서 충돌 해결

# 4. 해결 후 계속
git add .
git rebase --continue

# 5. 강제 푸시 (rebase 후)
git push --force-with-lease
```

### 5.2 충돌 마커 이해

```java
<<<<<<< HEAD
// 현재 브랜치 (feature) 코드
private String email;
=======
// 들어오는 변경 (develop) 코드
private String emailAddress;
>>>>>>> develop
```

### 5.3 주의사항

- `--force` 대신 `--force-with-lease` 사용 (안전)
- 공유 브랜치(`develop`, `main`)는 절대 force push 금지
- 충돌 해결 후 반드시 테스트 실행

---

## 6. 자주 사용하는 Git 명령어

### 6.1 기본 명령어

```bash
# 상태 확인
git status
git log --oneline -10
git diff

# 브랜치 관리
git branch -a                    # 모든 브랜치 목록
git branch -d feature/old        # 로컬 브랜치 삭제
git push origin -d feature/old   # 원격 브랜치 삭제

# 스태시 (임시 저장)
git stash                        # 변경사항 임시 저장
git stash pop                    # 임시 저장 복원
git stash list                   # 스태시 목록
```

### 6.2 실수 복구

```bash
# 마지막 커밋 메시지 수정 (push 전)
git commit --amend -m "새로운 메시지"

# 마지막 커밋 취소 (변경사항 유지)
git reset --soft HEAD~1

# 특정 커밋으로 되돌리기 (새 커밋 생성)
git revert <commit-hash>

# 파일 변경 취소
git checkout -- <file>
git restore <file>              # Git 2.23+
```

### 6.3 로그 확인

```bash
# 그래프로 보기
git log --graph --oneline --all

# 특정 파일 히스토리
git log --follow -p <file>

# 특정 작성자의 커밋
git log --author="name"

# 날짜 범위
git log --since="2025-01-01" --until="2025-01-31"
```

---

## 7. 협업 시나리오

### 7.1 시나리오 1: 다른 사람의 PR 리뷰

```bash
# 1. PR 브랜치 가져오기
git fetch origin pull/123/head:pr-123
git checkout pr-123

# 2. 로컬에서 테스트

# 3. GitHub에서 리뷰 코멘트 작성
```

### 7.2 시나리오 2: develop이 업데이트됨

```bash
# 방법 1: merge (히스토리 보존)
git checkout feature/my-feature
git merge develop

# 방법 2: rebase (깔끔한 히스토리)
git checkout feature/my-feature
git rebase develop
git push --force-with-lease
```

### 7.3 시나리오 3: 잘못된 브랜치에서 작업함

```bash
# 변경사항 스태시
git stash

# 올바른 브랜치로 이동
git checkout correct-branch

# 스태시 복원
git stash pop
```

---

## 8. Git Hooks (선택사항)

### 8.1 pre-commit 훅 예시

```bash
#!/bin/sh
# .git/hooks/pre-commit

# 테스트 실행
./gradlew test

if [ $? -ne 0 ]; then
    echo "테스트 실패! 커밋이 중단됩니다."
    exit 1
fi
```

### 8.2 commit-msg 훅 예시

```bash
#!/bin/sh
# .git/hooks/commit-msg

# 커밋 메시지 형식 검증
commit_regex='^(feat|fix|refactor|test|docs|style|chore|perf)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "커밋 메시지 형식이 올바르지 않습니다."
    echo "형식: <type>(<scope>): <subject>"
    exit 1
fi
```

---

## 9. 체크리스트

### 9.1 기능 개발 전

- [ ] develop 브랜치 최신화
- [ ] plan.md 작성 완료
- [ ] feature 브랜치 생성

### 9.2 커밋 전

- [ ] 테스트 통과 확인
- [ ] 불필요한 console.log / System.out 제거
- [ ] 커밋 메시지 컨벤션 준수

### 9.3 PR 전

- [ ] 자체 코드 리뷰
- [ ] 충돌 해결
- [ ] PR 설명 작성

---

## 10. 학습 포인트

### 10.1 왜 이런 워크플로우인가?

| 규칙 | 이유 |
|------|------|
| feature 브랜치 분리 | 독립적 개발, 쉬운 롤백 |
| Squash merge | 깔끔한 히스토리 유지 |
| 커밋 컨벤션 | 변경 이력 추적 용이 |
| PR 리뷰 필수 | 코드 품질 보장, 지식 공유 |

### 10.2 주의할 점

- **main/develop 직접 push 금지** → 항상 PR 통해서
- **force push는 개인 브랜치에서만**
- **커밋 전 항상 테스트 실행**

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
