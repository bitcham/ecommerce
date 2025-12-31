# Auth Service Implementation Plan

## Goal
Implement JWT-based authentication with registration, login, token refresh, logout, and email verification functionality.

## Approach

### Core Features
1. **Registration**: Create member with PENDING status, generate email verification token
2. **Login**: Validate credentials, check status, generate JWT access + refresh tokens
3. **Token Refresh**: Rotate refresh token, issue new access token
4. **Logout**: Revoke refresh token (single device or all devices)
5. **Email Verification**: Verify token, activate member account

### Security Measures
- Password hashing with BCrypt
- JWT with short-lived access tokens (15 min)
- Refresh token rotation on each use
- Device/IP tracking for security auditing
- Brute force protection via status checks

### Token Strategy
```
Access Token:
- Short-lived (15 minutes)
- Contains memberId, email, role
- Stored client-side (memory)

Refresh Token:
- Long-lived (7 days)
- Stored in database with device info
- Rotated on each refresh (one-time use)
```

## Trade-offs

| Decision | Trade-off |
|----------|-----------|
| JWT over Session | Stateless but requires token refresh mechanism |
| DB-stored refresh tokens | Revocation support but adds DB dependency |
| Token rotation | Security vs. complexity |
| Email verification required | Security vs. UX friction |

## Dependencies
- `MemberRepository` - Member persistence
- `RefreshTokenRepository` - Token persistence
- `EmailVerificationTokenRepository` - Verification token persistence
- `JwtTokenProvider` - JWT generation/validation
- `PasswordEncoder` - Password hashing

## Error Scenarios
- Invalid credentials → INVALID_CREDENTIALS
- Email not verified → EMAIL_NOT_VERIFIED
- Member suspended → MEMBER_SUSPENDED
- Member withdrawn → MEMBER_ALREADY_WITHDRAWN
- Invalid refresh token → REFRESH_TOKEN_NOT_FOUND
- Invalid verification token → TOKEN_INVALID
