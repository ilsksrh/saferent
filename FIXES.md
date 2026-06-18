# SafeRent — Все исправления и дополнения

---

## I. Исправления по результатам code review (/simplify)

### 1. Производительность

#### JwtUtil — кэширование SecretKey
**Файл:** `src/main/java/backend/saferent/util/JwtUtil.java`

Методы `generateToken` и `extractClaims` пересчитывали `SecretKey` на каждый HTTP-запрос.  
**Исправление:** добавлен `@PostConstruct init()` — ключ вычисляется один раз при запуске.

---

#### ContractServiceImpl — два запроса + сломанный distinct
**Файл:** `ContractServiceImpl.java`, `ContractRepository.java`

`getMyContracts()` делал два запроса и вызывал `.distinct()` на сущностях без `equals/hashCode` — дедупликация не работала.  
**Исправление:** добавлен JPQL-запрос `findByTenantOrLandlord` — один запрос, без distinct.

---

#### InspectionServiceImpl — мёртвая проверка статуса
**Файл:** `InspectionServiceImpl.java`

В `uploadCheckinPhoto()` после `getActiveContractOrThrow()` стояла повторная проверка статуса — мёртвый код.  
**Исправление:** лишний блок удалён.

---

### 2. Корректность логики

#### PaymentServiceImpl — недостижимая ветка в getEscrowStatus
**Файл:** `PaymentServiceImpl.java`

Ветка `else if (deposit.getStatus() == REFUNDED)` никогда не выполнялась (перед ней стояла проверка `depositPaid`, гарантирующая `status == PAID`).  
**Исправление:** логика переписана: `null → NOT_PAID`, `REFUNDED → RETURNED`, статус контракта → `ESCROW/TENANT/PENDING_REVIEW`.

---

### 3. Дублирование кода

#### AuthServiceImpl — дублированный builder AuthResponse
**Файл:** `AuthServiceImpl.java`

`register()` и `login()` содержали идентичный builder.  
**Исправление:** извлечён приватный `toAuthResponse(User, String)`. Добавлен нормальный `import java.time.LocalDateTime`.

---

### 4. Лишние комментарии

Удалены очевидные "what"-комментарии из:
- `BookingRepository`, `ChatRepository`, `FavoriteRepository`, `MessageRepository`, `NotificationRepository`, `PaymentRepository`, `ReviewRepository`, `InspectionPhotoRepository`
- `Payment.java` — trailing-комментарии на полях
- `EscrowStatusResponse.java`, `InspectionCompareResponse.java`
- `InspectionPhoto.java` — комментарий `// InspectionPhoto.java` в первой строке
- `PaymentServiceImpl.java` — два `// Берём requestedBy из токена`
- `ChatServiceImpl.java` — `// ChatServiceImpl.java — только метод sendMessage`

---

### 5. AI-сервис

#### ai_service/main.py — таймаут urlopen
Добавлен `timeout=10` секунд в `urllib.request.urlopen(url, timeout=10)`.  
Удалены все очевидные комментарии на русском.

---

---

## II. Реализация требований CLAUDE.md

### 1. pom.xml — полная переработка
**Файл:** `pom.xml`

**Что было не так:**
- Дублирующийся блок `maven-compiler-plugin`
- Несуществующие зависимости: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test`
- `mapstruct-processor` не включён в `annotationProcessorPaths` → MapStruct не генерировал имплементации
- Отсутствовали: MinIO, spring-boot-starter-mail

**Исправления:**
- Убраны дубли и несуществующие зависимости
- Добавлен `spring-boot-starter-test` (стандартный)
- Добавлены в `annotationProcessorPaths`: `mapstruct-processor`, `lombok`, `lombok-mapstruct-binding:0.2.0`
- Добавлен `io.minio:minio:8.5.11`
- Добавлен `spring-boot-starter-mail`
- Убран `spring-boot-admin` (не требуется по спецификации)

---

### 2. JpaConfig.java — добавлены аннотации
**Файл:** `src/main/java/backend/saferent/config/JpaConfig.java`

Был пустым классом без аннотаций.  
**Исправление:** добавлены `@Configuration` и `@EnableJpaAuditing`.

---

### 3. OpenApiConfig.java — Swagger с Bearer Auth
**Файл:** `src/main/java/backend/saferent/config/OpenApiConfig.java`

Был пустым классом.  
**Исправление:** добавлен `@Bean OpenAPI` с:
- Заголовком, описанием, версией
- Security scheme `BearerAuth` (HTTP Bearer / JWT)
- `addSecurityItem` — все эндпоинты показывают замочек в Swagger UI

---

### 4. SecurityConfig.java — недостающие публичные эндпоинты
**Файл:** `src/main/java/backend/saferent/config/SecurityConfig.java`

По спецификации (секция 7) должны быть публичными, но отсутствовали:
- `GET /api/v1/districts/{id}`
- `GET /api/v1/reviews/about/**`
- `GET /api/v1/reviews/rating/**`

**Исправление:** все три добавлены в `permitAll()`.

---

### 5. application.yaml — приведение к спецификации
**Файл:** `src/main/resources/application.yaml`

**Исправления:**
- `show-sql: true` → `show-sql: false`
- Добавлен `hibernate.format_sql: true`
- Добавлена секция `logging` (WARN для security, DEBUG для приложения)
- Добавлена секция `minio` с поддержкой env-переменных
- Добавлена секция `spring.mail` для email OTP
- `ai.service.url` вынесен в env-переменную с дефолтом

---

### 6. docker-compose.yml — добавлен MinIO и healthcheck для postgres
**Файл:** `docker-compose.yml`

**Исправления:**
- Добавлен сервис `minio` (image: minio/minio:latest, порты 9000/9001, healthcheck, volume)
- Добавлен healthcheck для `postgres` (pg_isready)
- `backend` теперь зависит от `postgres: condition: service_healthy` и `minio: condition: service_healthy`
- Добавлены env-переменные для MinIO в сервис backend
- `AI_SERVICE_URL` передаётся в backend

---

### 7. MinIO — загрузка файлов
**Новые файлы:**
- `config/MinioConfig.java` — бин `MinioClient`, `@PostConstruct` создаёт бакет с public-read политикой
- `service/FileStorageService.java` — интерфейс `upload(MultipartFile, folder)`
- `service/impl/FileStorageServiceImpl.java` — загружает в MinIO, возвращает публичный URL
- `controller/FileController.java` — `POST /api/v1/files/upload?folder=photos`

**Как использовать:**
1. `POST /api/v1/files/upload` — загрузить фото, получить `{ "url": "http://..." }`
2. Передать URL в `photoUrl` при создании инспекции/квартиры

---

### 8. Email OTP верификация
**Что заменяет:** ручной `/users/{id}/verify` без проверки

**Новые файлы:**
- `service/OtpService.java` — интерфейс `sendOtp(userId, email)`, `verifyOtp(userId, code)`
- `service/impl/OtpServiceImpl.java` — 6-значный код, хранится in-memory (ConcurrentHashMap), TTL 10 минут, отправляется через JavaMailSender

**Изменённые файлы:**
- `entity/User.java` — добавлено поле `email VARCHAR(100) UNIQUE`
- `dto/request/auth/RegisterRequest.java` — добавлено опциональное поле `email`
- `dto/request/user/CreateUserRequest.java` — добавлено `email`
- `dto/request/user/UpdateUserRequest.java` — добавлено `email`
- `dto/response/user/UserResponse.java` — добавлено `email`
- `mapper/UserMapper.java` — маппинг `email` во всех методах
- `service/UserService.java` — добавлены методы `requestOtp(UUID)`, `verifyUser(UUID, String code)` (старый `verifyUser(UUID)` заменён)
- `service/impl/UserServiceImpl.java` — реализация `requestOtp` и `verifyUser` с OTP-проверкой
- `service/impl/AuthServiceImpl.java` — передаёт `email` при построении User

**Эндпоинты (UserController):**

| Метод | URL | Описание |
|-------|-----|---------|
| `POST` | `/api/v1/users/{id}/verify/request` | Отправляет 6-значный код на email пользователя |
| `POST` | `/api/v1/users/{id}/verify?code=123456` | Проверяет код, устанавливает `verified=true` |

**Требование:** у пользователя должен быть установлен `email` (через регистрацию или `PUT /users/{id}`).

---

### 9. BookingServiceImpl — проверка verified
**Файл:** `src/main/java/backend/saferent/service/impl/BookingServiceImpl.java`

По спецификации (секция 5, BOOKINGS): `apartment MUST be verified=true, else 400 "Apartment must be verified first"`.  
Этой проверки не было.  
**Исправление:** добавлена проверка `if (!apartment.isVerified())` перед проверкой арендатора.

---

### 10. DistrictServiceImpl — пересчёт рейтинга района
**Файлы:** `DistrictServiceImpl.java`, `DistrictRatingRepository.java`

По спецификации (секция 5, DISTRICTS): после сохранения оценки нужно пересчитать `safetyScore` и `comfortScore` как AVG всех оценок.  
Пересчёта не было.

**Исправление:**
- В `DistrictRatingRepository` добавлены два JPQL-запроса с `COALESCE(AVG(...), 0)`
- В `DistrictServiceImpl.rateDistrict` после upsert вызывается пересчёт и сохранение района
- Метод помечен `@Transactional`

---

## III. Что намеренно НЕ менялось

| Тема | Причина |
|------|---------|
| Spring Boot 4.0.3 (в CLAUDE.md указан 3.2.x) | Проект уже работает на 4.0.x; даунгрейд сломает зависимости |
| `getUserOrThrow` дублируется во всех сервисах | Рефакторинг требует инъекции UserService повсюду → риск циклических зависимостей |
| `NotificationService.create()` — 6 параметров | Изменение публичного API; требует создания `NotificationRequest` DTO |
| `payDeposit` и `payRent` дублируют код | Сложная бизнес-логика, высокий риск регрессии |
| AI-вызовы последовательны в цикле (InspectionServiceImpl) | Требует `CompletableFuture` + разбиение `@Transactional` |
| OTP хранится in-memory | Для MVP достаточно; при рестарте сервера коды сбрасываются |
| server.servlet.context-path не добавлен | SecurityConfig и все контроллеры используют `/api/v1/...`; добавление context-path изменит все URL |

---

## IV. Порядок тестирования (расширенный Postman flow)

```
# Регистрация
POST /api/v1/auth/register
Body: { "phone": "+77001234567", "email": "tenant@test.kz", "name": "Tenant", "password": "pass123", "preferredRole": "TENANT" }

# Верификация через OTP
POST /api/v1/users/{tenantId}/verify/request   → письмо на email
POST /api/v1/users/{tenantId}/verify?code=123456

# Загрузка фото
POST /api/v1/files/upload
Body: form-data, file=<file>, folder=inspections
→ { "url": "http://localhost:9000/saferent-photos/inspections/uuid.jpg" }

# Остальной flow — без изменений (Postman flow из CLAUDE.md секция 14)
```
