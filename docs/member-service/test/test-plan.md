# Member Service Layer Test Plan

## Test Categories

### 1. Service Unit Tests (`MemberServiceTest.java`)

Uses Mockito to mock Repository layer.

#### 1.1 Register
| Test Case | Input | Expected |
|-----------|-------|----------|
| Register with valid data | Valid request | MemberResponse returned |
| Register with mismatched password | password != confirm | InvalidStateException |
| Register with duplicate email | Existing email | DuplicateResourceException |

#### 1.2 Get Member
| Test Case | Input | Expected |
|-----------|-------|----------|
| Get existing member | Valid ID | MemberResponse |
| Get non-existent member | Invalid ID | EntityNotFoundException |
| Get member detail with addresses | Valid ID | MemberDetailResponse with addresses |

#### 1.3 Search Members
| Test Case | Input | Expected |
|-----------|-------|----------|
| Search with no filter | Empty condition | PageResponse with members |
| Search by email | Email filter | Matching members |
| Search by name | Name filter | Matching members |
| Search with pagination | Page params | Correct page result |

#### 1.4 Update Profile
| Test Case | Input | Expected |
|-----------|-------|----------|
| Update with valid data | Valid request | Updated MemberResponse |
| Update non-existent member | Invalid ID | EntityNotFoundException |

#### 1.5 Change Password
| Test Case | Input | Expected |
|-----------|-------|----------|
| Change with correct current | Valid request | Success (void) |
| Change with wrong current | Wrong password | InvalidStateException |
| Change with mismatched new | new != confirm | InvalidStateException |

#### 1.6 Withdraw & Restore
| Test Case | Input | Expected |
|-----------|-------|----------|
| Withdraw active member | Valid ID | Success (void) |
| Restore withdrawn member | Withdrawn ID | MemberResponse with PENDING |
| Restore active member | Active ID | InvalidStateException |

#### 1.7 Address Operations
| Test Case | Input | Expected |
|-----------|-------|----------|
| Add address | Valid request | AddressResponse |
| Add address to non-existent member | Invalid member ID | EntityNotFoundException |
| Update address | Valid request | Updated AddressResponse |
| Remove address | Valid IDs | Success (void) |
| Set default address | Valid IDs | AddressResponse with isDefault=true |

### 2. Repository Tests (`MemberRepositoryTest.java`)

Uses @DataJpaTest with H2 in-memory database.

| Test Case | Input | Expected |
|-----------|-------|----------|
| Save and find by ID | New member | Member persisted and found |
| Find by email | Existing email | Member found |
| Find by email not found | Non-existent email | Empty Optional |
| Exists by email | Existing email | true |
| Exists by email not found | Non-existent email | false |
| Search with email filter | Email substring | Matching members |
| Search with name filter | Name substring | Matching members |
| Search excluding withdrawn | Mixed statuses | Only non-withdrawn |
| Search with pagination | Page 0, size 10 | Correct page |

### 3. Controller Integration Tests (`MemberControllerTest.java`)

Uses @WebMvcTest with MockMvc.

| Test Case | Endpoint | Expected Status |
|-----------|----------|-----------------|
| Register success | POST /members | 201 Created |
| Register invalid email format | POST /members | 400 Bad Request |
| Register missing required field | POST /members | 400 Bad Request |
| Get member success | GET /members/{id} | 200 OK |
| Get member not found | GET /members/{id} | 404 Not Found |
| Update profile success | PATCH /members/{id} | 200 OK |
| Change password success | PATCH /members/{id}/password | 204 No Content |
| Change password wrong current | PATCH /members/{id}/password | 400 Bad Request |
| Withdraw success | DELETE /members/{id} | 204 No Content |
| Add address success | POST /members/{id}/addresses | 201 Created |
| Add address invalid zip | POST /members/{id}/addresses | 400 Bad Request |

### 4. Mapper Tests (`MemberMapperTest.java`)

| Test Case | Input | Expected |
|-----------|-------|----------|
| Map Member to MemberResponse | Member entity | Correct MemberResponse |
| Map Member to MemberDetailResponse | Member with addresses | Correct detail with addresses |
| Map MemberAddress to AddressResponse | Address entity | Correct AddressResponse |
| Map list of members | List<Member> | List<MemberResponse> |

## Test Fixtures

```java
// Reuse existing MemberFixture
MemberFixture.createPendingMember()
MemberFixture.createActiveMember()
// etc.

// New service layer fixtures
MemberRequestFixture {
    createValidMemberCreateRequest()
    createInvalidMemberCreateRequest()
    createMemberUpdateRequest()
    createPasswordChangeRequest()
    createAddressCreateRequest()
}
```

## Mock Configuration

```java
@Mock MemberRepository memberRepository
@Mock MemberMapper memberMapper
@Mock PasswordEncoder passwordEncoder
@InjectMocks MemberServiceImpl memberService
```

## Test Execution Order

1. Mapper tests (no dependencies)
2. Service unit tests (mocked repository)
3. Repository tests (H2 database)
4. Controller integration tests (full context)
