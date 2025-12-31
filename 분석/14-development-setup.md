# 14. 개발 환경 설정 가이드 (Development Environment Setup)

> **학습 목표**: 로컬 개발 환경을 구축하고, 프로젝트를 빌드/실행할 수 있습니다.

---

## 1. 필수 설치 도구

### 1.1 설치 목록

| 도구 | 버전 | 용도 | 다운로드 |
|------|------|------|----------|
| **JDK** | 21+ | Java 런타임 | [Adoptium](https://adoptium.net/) |
| **IntelliJ IDEA** | 최신 | IDE | [JetBrains](https://www.jetbrains.com/idea/) |
| **PostgreSQL** | 15+ | 데이터베이스 | [PostgreSQL](https://www.postgresql.org/) |
| **Redis** | 7+ | 캐시/세션 | [Redis](https://redis.io/) |
| **Git** | 최신 | 버전 관리 | [Git](https://git-scm.com/) |
| **Docker** | 선택 | 컨테이너 | [Docker Desktop](https://www.docker.com/) |

### 1.2 JDK 설치 확인

```bash
# 버전 확인
java -version

# 예상 출력
openjdk version "21.0.x" ...
```

### 1.3 환경변수 설정 (Windows)

```
JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-21.x.x
Path += %JAVA_HOME%\bin
```

---

## 2. 프로젝트 클론 및 빌드

### 2.1 프로젝트 클론

```bash
git clone https://github.com/{repository}/ecommerce.git
cd ecommerce
```

### 2.2 Gradle 빌드

```bash
# Windows
gradlew.bat build

# Mac/Linux
./gradlew build

# 테스트 스킵 빌드 (빠른 확인)
gradlew.bat build -x test
```

### 2.3 빌드 성공 확인

```
BUILD SUCCESSFUL in XXs
```

---

## 3. 데이터베이스 설정

### 3.1 PostgreSQL 설정

**참조**: `application-local.yml:1-10`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5800/ecommerce
    username: ecommerce
    password: ecommerce
```

### 3.2 데이터베이스 생성

```sql
-- PostgreSQL 접속 후
CREATE DATABASE ecommerce;
CREATE USER ecommerce WITH PASSWORD 'ecommerce';
GRANT ALL PRIVILEGES ON DATABASE ecommerce TO ecommerce;
```

### 3.3 Docker로 실행 (권장)

```bash
# PostgreSQL
docker run --name postgres-ecommerce \
  -e POSTGRES_DB=ecommerce \
  -e POSTGRES_USER=ecommerce \
  -e POSTGRES_PASSWORD=ecommerce \
  -p 5800:5432 \
  -d postgres:15

# Redis
docker run --name redis-ecommerce \
  -p 6379:6379 \
  -d redis:7
```

### 3.4 Flyway 마이그레이션

**참조**: `application.yml:23-28`

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

- 애플리케이션 시작 시 자동 실행
- `src/main/resources/db/migration/` 위치에 SQL 파일

---

## 4. 프로파일 설정

### 4.1 프로파일 구조

```
src/main/resources/
├── application.yml           # 공통 설정
├── application-local.yml     # 로컬 개발 환경
├── application-prod.yml      # 운영 환경
└── application-test.yml      # 테스트 환경
```

### 4.2 프로파일별 특징

| 프로파일 | DB | 로그 레벨 | 용도 |
|---------|-----|---------|------|
| `local` | PostgreSQL (localhost) | DEBUG | 로컬 개발 |
| `test` | H2 (In-memory) | WARN | 테스트 |
| `prod` | PostgreSQL (운영) | INFO | 운영 배포 |

### 4.3 프로파일 활성화

```yaml
# application.yml
spring:
  profiles:
    active: local  # 기본값
```

```bash
# 실행 시 지정
java -jar app.jar --spring.profiles.active=prod
```

---

## 5. IntelliJ IDEA 설정

### 5.1 프로젝트 열기

1. **File → Open** → 프로젝트 폴더 선택
2. **Import Gradle Project** 선택
3. JDK 21 선택

### 5.2 필수 플러그인

| 플러그인 | 용도 |
|---------|------|
| Lombok | @Getter, @Builder 등 |
| MapStruct Support | Mapper 코드 생성 |
| JPA Buddy | 엔티티 관리 (선택) |

### 5.3 Lombok 설정

```
Settings → Build, Execution, Deployment
  → Compiler → Annotation Processors
  → Enable annotation processing ✅
```

### 5.4 코드 스타일 설정

```
Settings → Editor → Code Style → Java
  → Import scheme → IntelliJ IDEA style
```

---

## 6. 애플리케이션 실행

### 6.1 메인 클래스 실행

```
src/main/java/platform/ecommerce/EcommerceApplication.java
```

우클릭 → **Run 'EcommerceApplication'**

### 6.2 Gradle로 실행

```bash
gradlew.bat bootRun
```

### 6.3 실행 확인

```
Started EcommerceApplication in X.XXX seconds
```

---

## 7. API 테스트

### 7.1 Swagger UI 접속

**참조**: `application.yml:116-125`

```
http://localhost:8080/api/swagger-ui.html
```

### 7.2 Health Check

```bash
curl http://localhost:8080/api/actuator/health

# 응답
{"status":"UP"}
```

### 7.3 API 기본 경로

**참조**: `application.yml:62`

```yaml
server:
  servlet:
    context-path: /api
```

- 모든 API는 `/api` 접두사 사용
- 예: `http://localhost:8080/api/v1/members`

---

## 8. 주요 설정 파일 이해

### 8.1 application.yml 구조

**참조**: `application.yml:1-152`

```
┌─────────────────────────────────────────────────────────────┐
│                    application.yml 구조                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  spring:                                                    │
│  ├── jpa           # JPA/Hibernate 설정                     │
│  ├── flyway        # DB 마이그레이션                         │
│  ├── jackson       # JSON 직렬화                            │
│  ├── data.redis    # Redis 설정                             │
│  └── mail          # 메일 발송                              │
│                                                             │
│  server:           # 서버 설정 (포트, 컨텍스트 경로)          │
│  jwt:              # JWT 토큰 설정                           │
│  app:              # 애플리케이션 커스텀 설정                  │
│  logging:          # 로깅 설정                               │
│  management:       # Actuator 설정                          │
│  springdoc:        # Swagger 설정                           │
│  resilience4j:     # 서킷브레이커, Rate Limiter              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 주요 설정값

| 설정 | 값 | 설명 |
|------|-----|------|
| `server.port` | 8080 | 서버 포트 |
| `server.servlet.context-path` | /api | API 기본 경로 |
| `jwt.access-token-expiration` | 900000 (15분) | 액세스 토큰 만료 |
| `jwt.refresh-token-expiration` | 604800000 (7일) | 리프레시 토큰 만료 |
| `jpa.hibernate.ddl-auto` | validate | 스키마 검증만 |
| `hibernate.default_batch_fetch_size` | 100 | N+1 방지 배치 사이즈 |

---

## 9. 트러블슈팅

### 9.1 자주 발생하는 오류

| 오류 | 원인 | 해결 |
|------|------|------|
| `Connection refused` | DB 미실행 | PostgreSQL/Redis 실행 확인 |
| `Lombok not working` | Annotation Processing 미설정 | IntelliJ 설정 확인 |
| `Port already in use` | 8080 포트 사용 중 | 기존 프로세스 종료 |
| `Flyway migration failed` | 마이그레이션 충돌 | `flyway clean` 후 재실행 |

### 9.2 포트 충돌 해결

```bash
# Windows - 8080 포트 사용 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /PID <PID> /F
```

### 9.3 Gradle 캐시 삭제

```bash
# 캐시 삭제 후 재빌드
gradlew.bat clean build --refresh-dependencies
```

---

## 10. 개발 환경 체크리스트

### 10.1 최초 설정

- [ ] JDK 21 설치 및 환경변수 설정
- [ ] IntelliJ IDEA 설치
- [ ] Git 설치
- [ ] PostgreSQL 설치 (또는 Docker)
- [ ] Redis 설치 (또는 Docker)

### 10.2 프로젝트 설정

- [ ] 프로젝트 클론
- [ ] Gradle 빌드 성공
- [ ] IntelliJ에서 프로젝트 열기
- [ ] Lombok 플러그인 활성화
- [ ] Annotation Processing 활성화

### 10.3 실행 확인

- [ ] 데이터베이스 연결 성공
- [ ] 애플리케이션 시작
- [ ] Swagger UI 접속
- [ ] Health Check 응답

---

## 11. 학습 포인트

### 11.1 왜 이런 설정인가?

| 설정 | 이유 |
|------|------|
| `ddl-auto: validate` | 운영 환경 안전성 (Flyway로 스키마 관리) |
| `open-in-view: false` | 성능 최적화 (LazyLoading 명시적 관리) |
| `context-path: /api` | API 버전 관리, 리버스 프록시 연동 용이 |
| `default_batch_fetch_size: 100` | N+1 문제 완화 |

### 11.2 첫날 목표

1. 개발 환경 완료
2. 프로젝트 빌드 성공
3. Swagger UI에서 API 목록 확인
4. 간단한 API 호출 테스트

---

**작성일**: 2025-01-XX
**작성자**: Lead Engineer
**대상**: 신규 입사자, 인턴 엔지니어
