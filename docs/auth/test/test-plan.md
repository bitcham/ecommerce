# Auth Service Test Plan

## Unit Test Scenarios

### Registration
- [x] Should register member with valid request
- [x] Should throw exception for mismatched passwords
- [x] Should throw exception for duplicate email
- [x] Should create email verification token on registration

### Login
- [x] Should login successfully with valid credentials
- [x] Should throw exception for non-existent email
- [x] Should throw exception for wrong password
- [x] Should throw exception for unverified email
- [x] Should throw exception for suspended member
- [x] Should throw exception for withdrawn member
- [x] Should update last login timestamp
- [x] Should generate access and refresh tokens

### Token Refresh
- [x] Should refresh tokens with valid refresh token
- [x] Should rotate refresh token on refresh
- [x] Should throw exception for invalid refresh token
- [x] Should throw exception for expired refresh token
- [x] Should throw exception for suspended member

### Logout
- [x] Should revoke refresh token on logout
- [x] Should handle logout with non-existent token gracefully
- [x] Should revoke all tokens on logout all devices

### Email Verification
- [x] Should verify email with valid token
- [x] Should throw exception for invalid token
- [x] Should throw exception for expired token
- [x] Should activate member after verification

### Resend Verification
- [x] Should create new verification token
- [x] Should throw exception for non-existent email
- [x] Should throw exception for already verified email

## Test Fixtures Required
- Member in different statuses (PENDING, ACTIVE, SUSPENDED, WITHDRAWN)
- Valid/invalid refresh tokens
- Valid/invalid/expired verification tokens
- Mock HttpServletRequest

## Test Dependencies
- MockMvc or unit testing with mocks
- @ExtendWith(MockitoExtension.class)
- Mock repositories and JwtTokenProvider
