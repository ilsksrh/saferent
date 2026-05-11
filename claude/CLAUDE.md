# SafeRent — Backend Complete Specification
> **Java 21 + Spring Boot 3.x + PostgreSQL 15 + Spring Security + JWT**
> Read this file completely before writing any code. Every business rule matters.

---

## 1. Project Overview

SafeRent is a two-sided digital marketplace for **long-term residential rental in Kazakhstan**.
It solves one problem: **rental transactions in Kazakhstan are 70–77% informal**, causing deposit
disputes, fake listings, and zero legal protection.

### Three-party trust model
```
TENANT ──────────────── SafeRent (neutral) ──────────────── LANDLORD
         books & pays         holds escrow         owns apartment
         signs contract       verifies identity    signs contract
         uploads photos       runs AI analysis     receives deposit interest
```

### Four differentiating features (must ALL be implemented)
1. **Interest-bearing escrow** — deposit held by platform, landlord earns 12–14% p.a.
2. **AI photo analysis** — OpenCV + SSIM compares check-in vs check-out photos
3. **eGov EDS e-contracts** — legally binding, both parties sign (mock for MVP)
4. **NLP smart search** — sentence embeddings + cosine similarity (stub for MVP)

---

## 2. Technology Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| Language | Java 21 | Use records, pattern matching, sealed classes where appropriate |
| Framework | Spring Boot 3.2.x | Jakarta EE namespace |
| Security | Spring Security 6.x + JWT (jjwt 0.12.3) | Stateless, BCrypt passwords |
| Database | PostgreSQL 15 | UUID primary keys, JSONB for AI results |
| ORM | Spring Data JPA + Hibernate 6 | 3NF schema |
| API docs | springdoc-openapi 2.3.0 | Swagger UI at /swagger-ui.html |
| AI client | RestTemplate → Python FastAPI | Mock fallback if unavailable |
| Build | Maven | Java 21 target |

### pom.xml dependencies (must include all)
```xml
<!-- Spring Boot starters -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation

<!-- JWT -->
io.jsonwebtoken:jjwt-api:0.12.3
io.jsonwebtoken:jjwt-impl:0.12.3
io.jsonwebtoken:jjwt-jackson:0.12.3

<!-- Database -->
org.postgresql:postgresql

<!-- API docs -->
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0

<!-- Utilities -->
org.projectlombok:lombok
```

---

## 3. Project Structure

```
src/main/java/backend/saferent/
├── Application.java
├── client/
│   └── AiAnalysisClient.java          # HTTP client to Python AI service
├── config/
│   ├── JpaConfig.java                 # @EnableJpaAuditing
│   ├── JwtFilter.java                 # JWT validation filter
│   ├── OpenApiConfig.java             # Swagger + Bearer auth
│   ├── SecurityConfig.java            # Spring Security filter chain
│   └── WebConfig.java                 # CORS + RestTemplate bean
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ApartmentController.java
│   ├── BookingController.java
│   ├── ContractController.java
│   ├── PaymentController.java
│   ├── ChatController.java
│   ├── NotificationController.java
│   ├── InspectionController.java
│   ├── FavoriteController.java
│   ├── ReviewController.java
│   ├── DistrictController.java
│   ├── LandlordController.java
│   └── MessageController.java         # Empty — messages via ChatController
├── dto/
│   ├── request/
│   │   ├── auth/LoginRequest.java
│   │   ├── auth/RegisterRequest.java
│   │   ├── apartment/CreateApartmentRequest.java
│   │   ├── apartment/UpdateApartmentRequest.java
│   │   ├── booking/CreateBookingRequest.java
│   │   ├── chat/CreateChatRequest.java
│   │   ├── chat/SendMessageRequest.java
│   │   ├── contract/CreateContractRequest.java
│   │   ├── district/DistrictCreateRequest.java
│   │   ├── district/DistrictRatingRequest.java
│   │   ├── payment/CreatePaymentRequest.java
│   │   └── review/CreateReviewRequest.java
│   └── response/
│       ├── auth/AuthResponse.java
│       ├── apartment/ApartmentResponse.java
│       ├── booking/BookingResponse.java
│       ├── chat/ChatResponse.java
│       ├── chat/MessageResponse.java
│       ├── contract/ContractResponse.java
│       ├── district/DistrictResponse.java
│       ├── favorite/FavoriteResponse.java
│       ├── inspection/InspectionCompareResponse.java
│       ├── inspection/InspectionPhotoResponse.java
│       ├── notification/NotificationResponse.java
│       ├── payment/EscrowStatusResponse.java
│       ├── payment/PaymentResponse.java
│       ├── review/ReviewResponse.java
│       ├── review/UserRatingResponse.java
│       └── user/UserResponse.java
├── entity/
│   ├── AbstractEntity.java
│   ├── User.java
│   ├── Apartment.java
│   ├── ApartmentPhoto.java
│   ├── Booking.java
│   ├── Chat.java
│   ├── Contract.java
│   ├── District.java
│   ├── DistrictRating.java
│   ├── Favorite.java
│   ├── InspectionPhoto.java
│   ├── Message.java
│   ├── Notification.java
│   ├── Payment.java
│   ├── Report.java
│   └── Review.java
│   └── enums/
│       ├── ApartmentStatus.java       # ACTIVE, RENTED, ARCHIVED, DRAFT
│       ├── BookingStatus.java         # PENDING, APPROVED, REJECTED, CANCELLED
│       ├── ContractStatus.java        # DRAFT, PENDING, ACTIVE, COMPLETED, CANCELLED
│       ├── InspectionResult.java      # NO_DAMAGE, MINOR_DAMAGE, MAJOR_DAMAGE
│       ├── InspectionType.java        # CHECKIN, CHECKOUT
│       ├── NotificationType.java      # MESSAGE, CONTRACT, PAYMENT, BOOKING, REVIEW, SYSTEM
│       ├── PaymentMethod.java         # KASPI, HALYK, CARD, OTHER
│       ├── PaymentStatus.java         # PENDING, PAID, FAILED, REFUNDED
│       ├── PaymentType.java           # DEPOSIT, RENT
│       ├── PreferredRole.java         # TENANT, LANDLORD
│       ├── ReportReason.java          # SPAM, SCAM, FAKE_LISTING, ABUSE, OTHER
│       └── ReportStatus.java          # OPEN, IN_REVIEW, RESOLVED, REJECTED
├── exception/
│   ├── BadRequestException.java       # extends RuntimeException
│   ├── NotFoundException.java         # extends RuntimeException
│   └── GlobalExceptionHandler.java    # @RestControllerAdvice
├── mapper/
│   ├── ApartmentMapper.java
│   ├── BookingMapper.java
│   ├── ChatMapper.java
│   ├── ContractMapper.java
│   ├── DistrictMapper.java
│   ├── FavoriteMapper.java
│   ├── InspectionMapper.java
│   ├── LandlordMapper.java
│   ├── NotificationMapper.java
│   ├── PaymentMapper.java
│   ├── ReviewMapper.java
│   └── UserMapper.java
├── repository/
│   ├── ApartmentPhotoRepository.java
│   ├── ApartmentRepository.java
│   ├── BookingRepository.java
│   ├── ChatRepository.java
│   ├── ContractRepository.java
│   ├── DistrictRatingRepository.java
│   ├── DistrictRepository.java
│   ├── FavoriteRepository.java
│   ├── InspectionPhotoRepository.java
│   ├── MessageRepository.java
│   ├── NotificationRepository.java
│   ├── PaymentRepository.java
│   ├── ReportRepository.java
│   ├── ReviewRepository.java
│   └── UserRepository.java
├── service/
│   ├── AuthService.java (interface)
│   ├── ApartmentService.java (interface)
│   ├── BookingService.java (interface)
│   ├── ChatService.java (interface)
│   ├── ContractService.java (interface)
│   ├── DistrictService.java (interface)
│   ├── FavoriteService.java (interface)
│   ├── InspectionService.java (interface)
│   ├── LandlordService.java (interface)
│   ├── NotificationService.java (interface)
│   ├── PaymentService.java (interface)
│   ├── ReviewService.java (interface)
│   └── UserService.java (interface)
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── ApartmentServiceImpl.java
│       ├── BookingServiceImpl.java
│       ├── ChatServiceImpl.java
│       ├── ContractServiceImpl.java
│       ├── DistrictServiceImpl.java
│       ├── FavoriteServiceImpl.java
│       ├── InspectionServiceImpl.java
│       ├── LandlordServiceImpl.java
│       ├── NotificationServiceImpl.java
│       ├── PaymentServiceImpl.java
│       ├── ReviewServiceImpl.java
│       └── UserServiceImpl.java
└── util/
    ├── JwtUtil.java
    └── SecurityUtils.java
```

---

## 4. Database Schema

### AbstractEntity (base for all)
```
id          UUID PRIMARY KEY DEFAULT gen_random_uuid()
created_at  TIMESTAMP NOT NULL
updated_at  TIMESTAMP NOT NULL
```

### users
```
phone           VARCHAR(20) UNIQUE NOT NULL
name            VARCHAR(100) NOT NULL
password_hash   VARCHAR(255) NOT NULL       ← BCrypt encoded
preferred_role  VARCHAR(20)                 ← TENANT / LANDLORD
egov_id         VARCHAR(50)
verified        BOOLEAN DEFAULT false
avatar_url      VARCHAR(1024)
last_login_at   TIMESTAMP
login_provider  VARCHAR(50)
```

### district
```
name            VARCHAR(50) UNIQUE NOT NULL
safety_score    INTEGER DEFAULT 0           ← 0–10, recalculated from ratings
comfort_score   INTEGER DEFAULT 0           ← 0–10, recalculated from ratings
color_code      VARCHAR(7) NOT NULL         ← HEX format #RRGGBB
```

### district_rating
```
district_id     UUID FK→district NOT NULL
user_id         UUID FK→users NOT NULL
safety_rating   INTEGER 1–10 NOT NULL
comfort_rating  INTEGER 1–10 NOT NULL
UNIQUE(district_id, user_id)               ← one rating per user per district
```

### apartment
```
landlord_id     UUID FK→users NOT NULL
district_id     UUID FK→district NOT NULL
title           VARCHAR(150) NOT NULL
description     TEXT NOT NULL
address         VARCHAR(255) NOT NULL
price           DECIMAL(12,2) NOT NULL      ← monthly rent in KZT
area            DECIMAL(8,2) NOT NULL       ← m²
rooms           SMALLINT NOT NULL
available_from  DATE
verified        BOOLEAN DEFAULT false       ← must be true before booking
status          VARCHAR(20) DEFAULT 'ACTIVE' ← ACTIVE/RENTED/ARCHIVED/DRAFT
deleted_at      TIMESTAMP                   ← soft delete
```

### apartment_photo
```
apartment_id    UUID FK→apartment NOT NULL
url             VARCHAR(1024) NOT NULL
position        SMALLINT                    ← display order
```

### booking
```
apartment_id    UUID FK→apartment NOT NULL
tenant_id       UUID FK→users NOT NULL
start_date      DATE NOT NULL
end_date        DATE NOT NULL
message         TEXT
status          VARCHAR(20) NOT NULL        ← PENDING/APPROVED/REJECTED/CANCELLED
```

### contract
```
tenant_id           UUID FK→users NOT NULL
landlord_id         UUID FK→users NOT NULL
apartment_id        UUID FK→apartment NOT NULL
start_date          DATE NOT NULL
end_date            DATE NOT NULL
rent_amount         DECIMAL(12,2) NOT NULL  ← monthly KZT
deposit_amount      DECIMAL(12,2) NOT NULL  ← = 1 month rent for MVP
status              VARCHAR(20) NOT NULL    ← DRAFT/PENDING/ACTIVE/COMPLETED/CANCELLED
status_reason       TEXT
terms               TEXT
e_signature_tenant  TEXT                    ← "SIGNED_BY_{uuid}_AT_{datetime}" for MVP
e_signature_landlord TEXT
signed_at           TIMESTAMP
```

### payment
```
contract_id     UUID FK→contract NOT NULL
type            VARCHAR(20) NOT NULL        ← DEPOSIT / RENT
amount          DECIMAL(12,2) NOT NULL
method          VARCHAR(20)                 ← KASPI/HALYK/CARD/OTHER
status          VARCHAR(20) NOT NULL        ← PENDING/PAID/FAILED/REFUNDED
transaction_id  VARCHAR(100)               ← "TXN_XXXXXXXX" for MVP
paid_at         TIMESTAMP
```

### inspection_photo
```
contract_id         UUID FK→contract NOT NULL
type                VARCHAR(20) NOT NULL    ← CHECKIN / CHECKOUT
url                 VARCHAR(1024) NOT NULL
room_label          VARCHAR(50)             ← "kitchen","bedroom","bathroom","living_room","general"
ssim_score          DOUBLE                  ← filled after AI analysis
damage_description  TEXT
```

### chat
```
tenant_id       UUID FK→users NOT NULL
landlord_id     UUID FK→users NOT NULL
apartment_id    UUID FK→apartment NOT NULL
UNIQUE(tenant_id, landlord_id, apartment_id)  ← one chat per pair per apartment
```

### message
```
chat_id         UUID FK→chat NOT NULL
sender_id       UUID FK→users NOT NULL
text            TEXT NOT NULL
is_read         BOOLEAN DEFAULT false
```

### notification
```
user_id             UUID FK→users NOT NULL
title               VARCHAR(120) NOT NULL
message             TEXT
type                VARCHAR(20)             ← MESSAGE/CONTRACT/PAYMENT/BOOKING/REVIEW/SYSTEM
related_entity_id   UUID                    ← ID of related booking/contract/payment/chat
related_entity_type VARCHAR(50)             ← "BOOKING","CONTRACT","PAYMENT","CHAT","REVIEW"
is_read             BOOLEAN DEFAULT false
```

### favorite
```
user_id         UUID FK→users NOT NULL
apartment_id    UUID FK→apartment NOT NULL
UNIQUE(user_id, apartment_id)
```

### review
```
contract_id     UUID FK→contract NOT NULL
author_id       UUID FK→users NOT NULL
target_user_id  UUID FK→users NOT NULL
rating          SMALLINT 1–5 NOT NULL
comment         TEXT
UNIQUE(contract_id, author_id)             ← one review per author per contract
```

### report
```
reporter_id         UUID FK→users NOT NULL
target_user_id      UUID FK→users
apartment_id        UUID FK→apartment
message_id          UUID FK→message
reason              VARCHAR(20)             ← SPAM/SCAM/FAKE_LISTING/ABUSE/OTHER
description         TEXT
status              VARCHAR(20) DEFAULT 'OPEN'
moderator_id        UUID FK→users
resolution_comment  TEXT
resolved_at         TIMESTAMP
```

---

## 5. API Endpoints

### Base URL: `/saferent`
### Auth: `Authorization: Bearer <JWT>` on all endpoints except public ones

### Public endpoints (no token required)
```
POST  /auth/register
POST  /auth/login
GET   /apartments/active
GET   /apartments/{id}
GET   /districts
GET   /districts/{id}
GET   /reviews/about/{userId}
GET   /reviews/rating/{userId}
```

---

### AUTH
```
POST /auth/register
Body: { phone, name, password, preferredRole }
Returns: { accessToken, tokenType:"Bearer", userId, name, phone, role, verified }
Rules:
  - phone must be unique
  - password BCrypt encoded
  - returns JWT immediately on register

POST /auth/login
Body: { phone, password }
Returns: same as register
Rules:
  - wrong phone or password → 400 "Invalid phone or password"
  - updates lastLoginAt on success
```

---

### USERS
```
GET  /users/me              ← current user from JWT token
GET  /users/{id}
GET  /users/phone/{phone}
PUT  /users/{id}            Body: { name?, preferredRole?, egovId?, avatarUrl? }
POST /users/{id}/verify     ← sets verified=true (mock eGov for MVP)
POST /users/{id}/login      ← updates lastLoginAt
```

---

### DISTRICTS
```
POST /districts             Body: DistrictCreateRequest (admin only)
GET  /districts             ← all districts, used for map
GET  /districts/{id}
POST /districts/rate        Body: { districtId, safetyRating:1-10, comfortRating:1-10 }
                            ← userId from JWT
                            ← upsert: update if exists, create if not
                            ← after save, RECALCULATE district.safetyScore and comfortScore
                               as AVG of all ratings
```

---

### APARTMENTS
```
POST   /apartments
Body: { landlordId, districtId, title, description, address, price, area, rooms, availableFrom }
Rules:
  - landlordId from JWT (ignore body landlordId, use token)
  - verified defaults to false
  - status defaults to ACTIVE

GET    /apartments/active           ← only non-deleted ACTIVE apartments
GET    /apartments/{id}
GET    /apartments/landlord/{id}    ← all apartments by landlord (including deleted)
PUT    /apartments/{id}             ← partial update
DELETE /apartments/{id}             ← soft delete: set deletedAt + status=ARCHIVED
POST   /apartments/{id}/verify      ← sets verified=true, required before booking
```

---

### BOOKINGS
```
POST /bookings
Body: { apartmentId, startDate, endDate, message? }
tenantId from JWT
Rules:
  - apartment must exist and not be deleted
  - apartment MUST be verified=true, else 400 "Apartment must be verified first"
  - tenant cannot be the landlord of this apartment
  - no existing PENDING booking for same (apartment, tenant)
  - endDate must be after startDate
  - on success → notify landlord (NotificationType.BOOKING)

GET /bookings/my/tenant     ← all bookings where current user is tenant
GET /bookings/my/landlord   ← all bookings where current user is landlord (via apartments)
GET /bookings/{id}

POST /bookings/{id}/approve
landlordId from JWT
Rules:
  - current user must be landlord of the apartment
  - booking must be PENDING
  - set status=APPROVED
  - AUTO-CREATE Contract with status=DRAFT:
      tenant = booking.tenant
      landlord = apartment.landlord
      apartment = booking.apartment
      startDate/endDate from booking
      rentAmount = apartment.price
      depositAmount = apartment.price  (1 month = deposit for MVP)
      terms = "Standard SafeRent rental agreement."
  - notify tenant (BOOKING type, "Booking Approved! Please sign the contract.")

POST /bookings/{id}/reject
landlordId from JWT
Rules:
  - must be PENDING, must be landlord
  - set status=REJECTED
  - notify tenant

POST /bookings/{id}/cancel
tenantId from JWT
Rules:
  - must be tenant's own booking
  - cannot cancel APPROVED or REJECTED
  - set status=CANCELLED
```

---

### CONTRACTS
```
POST /contracts
Body: CreateContractRequest (manual creation, rarely used)
Rules:
  - validate dates
  - status=DRAFT

GET /contracts/my           ← contracts where current user is tenant OR landlord
GET /contracts/{id}

POST /contracts/{id}/sign
userId from JWT
Rules:
  - contract must not be CANCELLED or COMPLETED
  - user must be tenant or landlord of this contract
  - cannot sign twice ("Tenant has already signed")
  - signature string = "SIGNED_BY_{userId}_AT_{datetime}"
  - if only one signed → status=PENDING, notify the other party
  - if BOTH signed:
      → status=ACTIVE
      → signedAt = now()
      → apartment.status = RENTED
      → notify both (CONTRACT, "Contract is Active! Please pay the deposit.")

POST /contracts/{id}/cancel
userId from JWT, reason? (optional)
Rules:
  - must be party to contract
  - cannot cancel COMPLETED or already CANCELLED
  - if was ACTIVE → apartment.status = ACTIVE (unrent it)
  - set statusReason
```

---

### PAYMENTS & ESCROW
```
POST /payments/deposit
Body: { contractId, type:"DEPOSIT", amount, method }
payerId from JWT
Rules:
  - contract must be ACTIVE
  - payerId must be the tenant
  - no existing PAID deposit for this contract
  - amount MUST exactly equal contract.depositAmount
  - create Payment: status=PAID, transactionId="TXN_XXXXXXXX", paidAt=now()
  - notify landlord: "Deposit Received — safely in escrow"
  - notify tenant: "Deposit Paid Successfully — you can move in!"

POST /payments/rent
Body: { contractId, type:"RENT", amount, method }
payerId from JWT
Rules:
  - contract must be ACTIVE
  - payerId must be the tenant
  - deposit MUST be paid first (check existsByContractAndTypeAndStatus DEPOSIT PAID)
  - amount MUST exactly equal contract.rentAmount
  - create Payment: status=PAID
  - notify both parties

GET /payments/contract/{contractId}     ← payment history ordered by createdAt DESC
GET /payments/escrow/{contractId}       ← escrow status with human-readable note

POST /payments/escrow/{contractId}/release/tenant
requestedBy from JWT (tenant or landlord can trigger)
Rules:
  - deposit must be in PAID status
  - set deposit.status = REFUNDED
  - set deposit.transactionId = "REFUND_TO_TENANT_TXN_XXXXXXXX"
  - contract.status = COMPLETED
  - notify tenant: "Deposit Returned!"

POST /payments/escrow/{contractId}/release/landlord
requestedBy from JWT (must be landlord)
Rules:
  - deposit must be in PAID status
  - set deposit.status = REFUNDED
  - deposit.transactionId = "TRANSFER_TO_LANDLORD_TXN_XXXXXXXX"
  - contract.status = COMPLETED
  - notify both parties
```

### Escrow status logic
```
if deposit not paid         → destination="NOT_PAID"
if deposit.status=REFUNDED  → destination="RETURNED"
if contract ACTIVE          → destination="ESCROW" (safe, earning interest)
if contract COMPLETED       → destination="TENANT"
if contract CANCELLED       → destination="PENDING_REVIEW"
```

---

### CHAT
```
POST /chats
Body: { tenantId, landlordId, apartmentId }
Rules:
  - cannot chat with yourself
  - landlordId must be the actual owner of apartmentId
  - IDEMPOTENT: if chat already exists, return existing
  - creates chat with tenant+landlord+apartment

GET  /chats/my              ← all chats where current user is tenant or landlord
                              includes lastMessage preview, unreadCount

POST /chats/{chatId}/messages
Body: { text }
senderId from JWT
Rules:
  - sender must be tenant or landlord of this chat
  - text cannot be blank
  - BEFORE saving: auto-mark all incoming unread messages as read
    (messages from the other party that are unread)
  - save new message with isRead=false
  - notify recipient: "New message from {senderName}"

GET  /chats/{chatId}/messages   ← history ordered by createdAt ASC
                                  requester must be party to chat

POST /chats/{chatId}/read       ← mark all messages from other party as isRead=true
                                  userId from JWT

GET  /chats/unread              ← count of unread messages across all chats for current user
```

---

### NOTIFICATIONS
```
GET  /notifications/my          ← all, ordered createdAt DESC
GET  /notifications/unread      ← count of isRead=false
POST /notifications/{id}/read   ← mark one as read (must be owner)
POST /notifications/read-all    ← bulk mark all as read for current user
```

---

### INSPECTION (Check-in / Check-out Photos + AI)
```
POST /inspections/{contractId}/checkin
Params: photoUrl, roomLabel? (default "general")
uploadedBy from JWT
Rules:
  - contract must be ACTIVE
  - uploader must be the tenant
  - multiple photos allowed (one per room label per type)
  - notify landlord: "Check-in Photos Uploaded"

POST /inspections/{contractId}/checkout
Params: photoUrl, roomLabel? (default "general")
uploadedBy from JWT
Rules:
  - uploader must be the tenant
  - MUST have at least one CHECKIN photo first
  - notify landlord: "Check-out Photos Uploaded. AI analysis can now be started."

POST /inspections/{contractId}/compare
requestedBy from JWT (tenant or landlord)
Algorithm:
  1. Load all CHECKIN and CHECKOUT photos
  2. Group by roomLabel
  3. For each room present in both groups:
     a. Call Python AI service: POST {aiServiceUrl}/compare { beforeUrl, afterUrl }
     b. Save ssimScore + damageDescription to checkout photo entity
     c. If score < 0.92 → add to damagedRooms list
  4. Calculate averageSsimScore across all rooms
  5. Determine InspectionResult:
     - avgScore >= 0.92 → NO_DAMAGE → deposit returns to tenant → notify both
     - avgScore 0.70–0.92 → MINOR_DAMAGE → manual review → notify both
     - avgScore < 0.70 → MAJOR_DAMAGE → deposit to landlord → notify both
  6. Return InspectionCompareResponse with all room scores and summary
  
  If AI service unavailable: mock result ssimScore=0.95, damageRegionCount=0

GET /inspections/{contractId}   ← all photos for this contract
```

---

### FAVORITES
```
POST /favorites/toggle?apartmentId={id}
userId from JWT
Rules:
  - cannot favorite own apartment
  - if exists → delete and return "REMOVED"
  - if not → create and return "ADDED"

GET  /favorites/my          ← current user's favorites
GET  /favorites/check?apartmentId={id}  ← boolean: is this apartment favorited?
```

---

### REVIEWS
```
POST /reviews
Body: { contractId, targetUserId, rating:1-5, comment? }
authorId from JWT
Rules:
  - contract must be COMPLETED
  - author must be tenant or landlord of this contract
  - targetUser must be the OTHER party in the contract
  - author cannot review themselves
  - UNIQUE: one review per (contractId, authorId)
  - notify targetUser: "New Review from {authorName}: {rating} stars"

GET /reviews/about/{userId}     ← reviews about this user
GET /reviews/by/{userId}        ← reviews written by this user
GET /reviews/rating/{userId}    ← { averageRating, totalReviews, ratingLabel }
  ratingLabel:
    >= 4.5 → "Excellent"
    >= 3.5 → "Good"
    >= 2.5 → "Average"
    > 0    → "Poor"
    0      → "No reviews yet"
```

---

### LANDLORDS
```
GET  /landlords/{userId}                ← landlord profile with apartment count
GET  /landlords/{userId}/apartments     ← summary list of landlord's apartments
POST /landlords/{userId}/promote        ← set role=LANDLORD
```

---

## 6. Business Rules Summary

### Apartment lifecycle
```
DRAFT → (verify) → ACTIVE → (booking approved + both sign) → RENTED → (contract completed) → ACTIVE
                           ↓ (soft delete)
                        ARCHIVED
```

### Booking → Contract flow
```
Tenant creates BOOKING (PENDING)
  ↓
Landlord APPROVES booking
  ↓ AUTO-CREATES Contract (DRAFT)
  ↓ Notifies tenant
  ↓
Tenant SIGNS contract → PENDING (one signed)
  ↓
Landlord SIGNS contract → ACTIVE (both signed)
  ↓ apartment.status = RENTED
  ↓
Tenant pays DEPOSIT → escrow PAID
  ↓
Tenant pays RENT (monthly)
  ↓
Tenant uploads CHECKIN photos
  ↓
(months later) Tenant uploads CHECKOUT photos
  ↓
AI COMPARE runs → result
  ↓
SSIM >= 0.92 → releaseToTenant → contract COMPLETED
0.70-0.92    → manual review
< 0.70       → releaseToLandlord → contract COMPLETED
  ↓
Both parties leave REVIEW
```

### Notification triggers
| Event | Who gets notified | Type |
|-------|------------------|------|
| Booking created | Landlord | BOOKING |
| Booking approved | Tenant | BOOKING |
| Booking rejected | Tenant | BOOKING |
| One party signed | Other party | CONTRACT |
| Both signed (ACTIVE) | Both | CONTRACT |
| Deposit paid | Both | PAYMENT |
| Rent paid | Both | PAYMENT |
| Check-in uploaded | Landlord | CONTRACT |
| Check-out uploaded | Landlord | CONTRACT |
| AI: no damage | Both | PAYMENT |
| AI: minor damage | Both | SYSTEM |
| AI: major damage | Both | PAYMENT |
| Deposit returned | Tenant | PAYMENT |
| Deposit to landlord | Both | PAYMENT |
| New chat message | Recipient | MESSAGE |
| Review received | Target user | REVIEW |

---

## 7. Security Configuration

### Public endpoints (no JWT)
```
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/apartments/active
GET  /api/v1/apartments/{id}
GET  /api/v1/districts
GET  /api/v1/districts/{id}
GET  /api/v1/reviews/about/{userId}
GET  /api/v1/reviews/rating/{userId}
/swagger-ui/**
/v3/api-docs/**
```

### JWT Filter logic
```
1. Read Authorization header
2. Extract Bearer token
3. Validate with JwtUtil.isValid()
4. Extract userId from token
5. Load User from UserRepository
6. Create UsernamePasswordAuthenticationToken with role "ROLE_{TENANT|LANDLORD}"
7. Set in SecurityContextHolder
```

### SecurityUtils
```java
// Used in all service methods to get current user
getCurrentUser()   → User entity from SecurityContextHolder
getCurrentUserId() → UUID
```

---

## 8. AI Service (Python FastAPI)

### Endpoint: POST /compare
```json
Request:  { "beforeUrl": "https://...", "afterUrl": "https://..." }
Response: { "ssimScore": 0.9234, "damageRegionCount": 2, "damageDescription": "..." }
```

### SSIM thresholds
```
>= 0.92 → NO_DAMAGE    → full deposit to tenant
0.70-0.92 → MINOR_DAMAGE → human moderator review
< 0.70  → MAJOR_DAMAGE → deposit to landlord
```

### Mock fallback (when Python service down)
```
ssimScore = 0.95
damageRegionCount = 0
damageDescription = "AI service unavailable — mock result: no damage detected"
```

---

## 9. application.yaml
```yaml
spring:
  application:
    name: saferent-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/saferent_db
    username: postgres
    password: sara1234
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: saferent_super_secret_key_must_be_at_least_32_chars
  expiration: 86400000  # 24 hours in ms

ai:
  service:
    url: http://localhost:8000

logging:
  level:
    org.springframework.security: WARN
    backend.saferent: DEBUG
```

---

## 10. CORS Configuration
```java
// Allow from:
"http://localhost:3000"   // React dev server (Vite default)
"http://localhost:5173"   // Alternative Vite port
"http://localhost:8081"   // iOS Expo

// Methods: GET, POST, PUT, DELETE, OPTIONS
// Headers: *
// Credentials: true
```

---

## 11. Error Response Format
```json
{
  "timestamp": "2026-04-26T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Apartment not found: {id}"
}
```

| Exception | HTTP Status |
|-----------|------------|
| NotFoundException | 404 |
| BadRequestException | 400 |
| IllegalArgumentException | 400 |
| IllegalStateException | 409 |
| MethodArgumentNotValidException | 400 |
| Exception (generic) | 500 |


NEED to add following functionalities:
**File upload** = now photoUrl is passed as string parameter, MUST REALIZE minio upload
**OTP SMS** = not implemented, /users/{id}/verify is manual trigger, lets make sms code or email code
---

## 12. Known MVP Limitations (document in diploma 3.4)
1. **eGov EDS** = mock string signature, not real cryptographic signing
2. **Kaspi/Halyk Pay** = mock transactionId, no real API call
3. **NLP smart search** = not implemented (stub endpoint, returns regular list)
6. **Interest accrual** = not calculated (noted as 12-14% p.a. in diploma, not coded)

---

## 13. Docker Compose
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: saferent_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: sara1234
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  ai-service:
    build:
      context: ./ai_service
      dockerfile: Dockerfile
    ports:
      - "8000:8000"

  backend:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/saferent_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: sara1234
      JWT_SECRET: saferent_super_secret_key_must_be_at_least_32_chars
      AI_SERVICE_URL: http://ai-service:8000

volumes:
  postgres_data:
```

---

## 14. Testing (Postman flow)

```
1. POST /auth/register (LANDLORD) → save landlordId, landlordToken
2. POST /auth/register (TENANT) → save tenantId, tenantToken
3. POST /districts (landlordToken) → save districtId
4. POST /apartments (landlordToken, body: landlordId from token) → save apartmentId
5. POST /apartments/{id}/verify (landlordToken)
6. POST /bookings (tenantToken, body: apartmentId, dates) → save bookingId
7. POST /bookings/{id}/approve (landlordToken) → save contractId
8. GET /contracts/my (tenantToken) → find contractId
9. POST /contracts/{id}/sign (tenantToken) → PENDING
10. POST /contracts/{id}/sign (landlordToken) → ACTIVE, apartment RENTED
11. POST /payments/deposit (tenantToken, amount=contract.depositAmount) → PAID
12. POST /payments/rent (tenantToken, amount=contract.rentAmount)
13. GET /payments/escrow/{contractId} → destination=ESCROW
14. POST /inspections/{contractId}/checkin (tenantToken, photoUrl=..., roomLabel=kitchen)
15. POST /inspections/{contractId}/checkout (tenantToken, photoUrl=..., roomLabel=kitchen)
16. POST /inspections/{contractId}/compare (tenantToken) → InspectionResult
17. POST /payments/escrow/{contractId}/release/tenant (landlordToken) → COMPLETED
18. POST /reviews (tenantToken, targetUserId=landlordId, rating=5, contractId)
19. GET /reviews/rating/{landlordId} → averageRating:5.0, label:"Excellent"
20. GET /notifications/my (landlordToken) → see all events
```
