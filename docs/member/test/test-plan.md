# Member Module Test Plan

## Test Categories

### 1. Domain Unit Tests (`MemberTest.java`)

#### 1.1 Member Creation
| Test Case | Input | Expected |
|-----------|-------|----------|
| Create member with valid data | email, password, name, phone | Status=PENDING, Role=CUSTOMER, emailVerified=false |
| Default values set correctly | Valid member | isDeleted=false, addresses=empty |

#### 1.2 Email Verification
| Test Case | Input | Expected |
|-----------|-------|----------|
| Verify pending member | PENDING member | Status=ACTIVE, emailVerified=true |
| Verify already verified member | ACTIVE member | InvalidStateException |

#### 1.3 Status Transitions
| Test Case | Input | Expected |
|-----------|-------|----------|
| Suspend active member | ACTIVE member | Status=SUSPENDED |
| Suspend withdrawn member | WITHDRAWN member | InvalidStateException |
| Activate suspended member | SUSPENDED member | Status=ACTIVE |
| Withdraw active member | ACTIVE member | Status=WITHDRAWN, deletedAt!=null |
| Restore withdrawn member | WITHDRAWN member | Status=PENDING, deletedAt=null |
| Restore active member | ACTIVE member | InvalidStateException |

#### 1.4 Login Eligibility
| Test Case | Input | Expected |
|-----------|-------|----------|
| Active member can login | ACTIVE member | canLogin()=true |
| Pending member cannot login | PENDING member | canLogin()=false |
| Suspended member cannot login | SUSPENDED member | canLogin()=false |
| Withdrawn member cannot login | WITHDRAWN member | canLogin()=false |

#### 1.5 Profile Update
| Test Case | Input | Expected |
|-----------|-------|----------|
| Update name and phone | newName, newPhone | Updated values |
| Update with null name | null, newPhone | Name unchanged |
| Update with blank name | "", newPhone | Name unchanged |

#### 1.6 Role Change
| Test Case | Input | Expected |
|-----------|-------|----------|
| Upgrade customer to seller | CUSTOMER | Role=SELLER |
| Upgrade admin to seller | ADMIN | InvalidStateException |

#### 1.7 Address Management
| Test Case | Input | Expected |
|-----------|-------|----------|
| Add first address | Valid address data | addresses.size=1, isDefault=true |
| Add address as non-default | isDefault=false | Still becomes default (first address) |
| Add second address as default | isDefault=true | Previous default unset |
| Add 11th address | 10 existing | InvalidStateException (limit exceeded) |
| Remove address | Valid addressId | addresses.size decreased |
| Remove default address | Default address | Next address becomes default |
| Remove non-existent address | Invalid addressId | InvalidStateException |

### 2. MemberAddress Unit Tests (`MemberAddressTest.java`)

| Test Case | Input | Expected |
|-----------|-------|----------|
| Create address | Valid data | All fields set correctly |
| Update address | New values | Fields updated |
| Get full address with detail | All fields | "(zipCode) address detail" |
| Get full address without detail | No detail | "(zipCode) address" |

### 3. Service Unit Tests (`MemberServiceTest.java`)

#### 3.1 Register
| Test Case | Input | Expected |
|-----------|-------|----------|
| Register with valid data | Valid request | MemberResponse returned |
| Register with mismatched password | password != confirm | InvalidStateException |
| Register with duplicate email | Existing email | DuplicateResourceException |

#### 3.2 Get Member
| Test Case | Input | Expected |
|-----------|-------|----------|
| Get existing member | Valid ID | MemberResponse |
| Get non-existent member | Invalid ID | EntityNotFoundException |

#### 3.3 Search Members
| Test Case | Input | Expected |
|-----------|-------|----------|
| Search with no filter | Empty condition | All non-withdrawn members |
| Search by email | Email filter | Matching members |
| Search by name | Name filter | Matching members |

#### 3.4 Update Profile
| Test Case | Input | Expected |
|-----------|-------|----------|
| Update with valid data | Valid request | Updated MemberResponse |
| Update non-existent member | Invalid ID | EntityNotFoundException |

#### 3.5 Change Password
| Test Case | Input | Expected |
|-----------|-------|----------|
| Change with correct current | Valid request | Password changed |
| Change with wrong current | Wrong password | InvalidStateException |
| Change with mismatched new | new != confirm | InvalidStateException |

#### 3.6 Withdraw & Restore
| Test Case | Input | Expected |
|-----------|-------|----------|
| Withdraw active member | Valid ID | Status=WITHDRAWN |
| Restore withdrawn member | Withdrawn ID | Status=PENDING |
| Restore active member | Active ID | InvalidStateException |

#### 3.7 Address Operations
| Test Case | Input | Expected |
|-----------|-------|----------|
| Add address | Valid request | AddressResponse |
| Update address | Valid request | Updated AddressResponse |
| Remove address | Valid IDs | Address removed |
| Set default address | Valid IDs | isDefault=true, others=false |

### 4. Repository Tests (`MemberRepositoryTest.java`)

| Test Case | Input | Expected |
|-----------|-------|----------|
| Find by email | Existing email | Member found |
| Find by email not found | Non-existent email | Empty Optional |
| Exists by email | Existing email | true |
| Search with conditions | Various filters | Correct results |

### 5. Controller Integration Tests (`MemberControllerTest.java`)

| Test Case | Endpoint | Expected Status |
|-----------|----------|-----------------|
| Register success | POST /members | 201 Created |
| Register invalid email | POST /members | 400 Bad Request |
| Get member success | GET /members/{id} | 200 OK |
| Get member not found | GET /members/{id} | 404 Not Found |
| Update profile success | PATCH /members/{id} | 200 OK |
| Change password success | PATCH /members/{id}/password | 204 No Content |
| Withdraw success | DELETE /members/{id} | 204 No Content |
| Add address success | POST /members/{id}/addresses | 201 Created |

## Test Fixtures Required

```java
MemberFixture {
    DEFAULT_EMAIL = "test@example.com"
    DEFAULT_PASSWORD = "encodedPassword123"
    DEFAULT_NAME = "TestUser"
    DEFAULT_PHONE = "010-1234-5678"

    createPendingMember()
    createPendingMember(email)
    createActiveMember()
    createActiveMember(email)
    createSuspendedMember()
    createWithdrawnMember()
    createSellerMember()
}
```

## Edge Cases to Cover

1. **Concurrent address addition**: Two requests adding addresses simultaneously
2. **Empty string vs null**: Name update with "" vs null
3. **Unicode names**: Korean/Chinese/Japanese characters in name
4. **Long strings**: Max length validation for all fields
5. **SQL injection**: Special characters in search conditions

## Test Execution Order

1. Domain unit tests (no dependencies)
2. Repository tests (requires test DB)
3. Service unit tests (mocked dependencies)
4. Controller integration tests (full context)
